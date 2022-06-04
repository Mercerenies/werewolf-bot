
package com.mercerenies.werewolf
package state

import id.Id
import util.TextDecorator.*
import util.Emoji
import logging.Logging
import logging.LogEither.*
import name.{NameProvider, BaseNameProvider, DisplayNameProvider}

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.{TextChannel, Channel}
import org.javacord.api.entity.user.User
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.Nameable
import org.javacord.api.entity.Mentionable
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder

import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.jdk.CollectionConverters.*
import scala.concurrent.{Future, ExecutionContext}

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

// Parent trait for the states a game can be in.
final class SignupState(
  override val channelId: Id[TextChannel & Nameable],
  override val hostId: Id[User],
  private val gameStartMessageId: Id[Message],
  private val signupsMessageId: Id[Message],
)(
  using ExecutionContext,
) extends GameState with Logging[SignupState] {

  import scalaz.EitherT.eitherTHoist

  private val noChannelError: String =
    s"Channel ${channelId} should be hosting a sign-up but does not exist"

  private val channelIdChangedPanic: String =
    s"Named TextChannel ${channelId} has changed identity and is no longer valid"

  private val noServerError: String =
    s"Could not identify server for ${channelId}"

  private def getChannel[M[_]: Monad](api: DiscordApi): EitherT[String, M, TextChannel & Nameable] =
    val chan: Option[Channel] = api.getChannelById(channelId.toLong).toScala
    EitherT.fromOption(noChannelError)(chan.point).map {
      case ch: (TextChannel & Nameable) => ch
      // Note that the above is an error we can expect (user deleted
      // the channel at a bad time), but if the channel still *exists*
      // but has changed identity, that should never happen and we can
      // just fail fast.
      case ch => throw Exception(channelIdChangedPanic)
    }

  private def getGameStartMessage(api: DiscordApi): EitherT[String, Future, Message] =
    for {
      channel <- getChannel(api)
      messageId <- api.getMessageById(gameStartMessageId.toLong, channel).asScala.liftM
    } yield {
      messageId
    }

  private def getSignupsMessage(api: DiscordApi): EitherT[String, Future, Message] =
    for {
      channel <- getChannel(api)
      messageId <- api.getMessageById(signupsMessageId.toLong, channel).asScala.liftM
    } yield {
      messageId
    }

  private def getNameProvider(api: DiscordApi): EitherT[String, Future, NameProvider] =
    getGameStartMessage(api).map { message =>
      message.getServer.toScala match {
        case None => {
          logger.warn(noServerError)
          BaseNameProvider
        }
        case Some(server) => {
          DisplayNameProvider(server)
        }
      }
    }

  def getSignups(api: DiscordApi): Future[collection.Seq[User]] =
    val r = for {
      message <- getGameStartMessage(api)
      joinReactions <- SignupState.getJoinReactions(message).liftM
    } yield {
      joinReactions.filter { user => !user.isBot }
    }
    // In case of error, log and return Nil
    r.warningToLogger(logger).map { _.getOrElse(Nil) }

  private def getSignupNames(api: DiscordApi): Future[collection.Seq[String]] =
    val r = for {
      nameProvider <- getNameProvider(api)
      users <- getSignups(api).liftM
    } yield {
      users.map { nameProvider.getNameOf(_) }
    }
    // In case of error, log and return Nil
    r.warningToLogger(logger).map { _.getOrElse(Nil) }

  def updateSignupList(api: DiscordApi): Future[Unit] =
    val r = for {
      signupsMessage <- getSignupsMessage(api)
      users <- getSignupNames(api).map { _.sorted }.liftM
      _ <- signupsMessage.edit(SignupState.getSignupMessage(users)).asScala.liftM
    } yield {
      ()
    }
    // In case of error, log and return ()
    r.warningToLogger(logger).map { _.getOrElse(()) }

  override def onReactionsUpdated(mgr: GamesManager, message: Message): Unit =
    if (message.getId == gameStartMessageId.toLong) {
      updateSignupList(mgr.api)
    }

}

object SignupState {

  private val joinEmoji: String = Emoji.Clipboard

  private def getJoinReactions(message: Message)(using ExecutionContext): Future[collection.Seq[User]] =
    message.getReactionByEmoji(joinEmoji).toScala match {
      case None => Future.successful(Nil)
      case Some(reaction) => reaction.getUsers().asScala.map(_.asScala)
    }

  private def getSignupMessage(usernames: collection.Seq[String]): String =
    if (usernames.isEmpty) {
      "Signups: (None)"
    } else {
      "Signups: " + usernames.mkString(", ")
    }

  private def gameStartMessage(host: Mentionable): String =
    s"${host.getMentionTag} has started a game of One Night Ultimate Werewolf in this channel. " +
    bold("Signups are open.") + s" React to this post with ${joinEmoji} to join the game."

  def createGame(channel: TextChannel & Nameable, host: User)(using ExecutionContext): Future[SignupState] = {
    val api = channel.getApi
    val text = gameStartMessage(host)
    for {
      startMessage <- channel.sendMessage(text).asScala
      signupsMessage <- channel.sendMessage("(Please wait)").asScala
      _ <- startMessage.addReaction(joinEmoji).asScala
      state = SignupState(Id(channel), Id(host), Id(startMessage), Id(signupsMessage))
      _ <- state.updateSignupList(api)
    } yield {
      state
    }
  }

}
