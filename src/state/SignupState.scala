
package com.mercerenies.werewolf
package state

import util.TextDecorator.*
import util.Emoji
import logging.Logging

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.Nameable
import org.javacord.api.entity.Mentionable
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder

import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.jdk.CollectionConverters.*
import scala.concurrent.{Future, ExecutionContext}

// Parent trait for the states a game can be in.
final class SignupState(
  override val channelId: Id[TextChannel & Nameable],
  override val hostId: Id[User],
  private val gameStartMessageId: Id[Message],
  private val signupsMessageId: Id[Message],
)(
  using ExecutionContext,
) extends GameState with Logging[SignupState] {

  private def getChannel(api: DiscordApi): Option[TextChannel & Nameable] =
    api.getChannelById(channelId.toLong).toScala.map {
      case ch: (TextChannel & Nameable) => ch
      case ch => throw Exception(s"Named TextChannel ${ch} has changed identity and is no longer valid")
    }

  // Yes, I realize Option[Future[A]] is a weird type, but the Option
  // part is determined before we invoke a Future. It's easy to
  // convert this to Future[Option[A]], but the reverse translation is
  // hard to come by.

  private def getGameStartMessage(api: DiscordApi): Option[Future[Message]] =
    getChannel(api).map { channel => api.getMessageById(gameStartMessageId.toLong, channel).asScala }

  private def getSignupsMessage(api: DiscordApi): Option[Future[Message]] =
    getChannel(api).map { channel => api.getMessageById(signupsMessageId.toLong, channel).asScala }

  def getSignups(api: DiscordApi): Future[collection.Seq[User]] =
    getSignupsMessage(api) match {
      case None => {
        logger.warn(s"Channel ${channelId} should be hosting a sign-up but does not exist")
        Future.successful(Nil)
      }
      case Some(m) => {
        for {
          signupsMessage <- m
          joinReactions <- SignupState.getJoinReactions(signupsMessage)
        } yield {
          joinReactions.filter { user => !user.isBot }
        }
      }
    }

  private def getSignupNames(api: DiscordApi): Future[collection.Seq[String]] =
    getGameStartMessage(api) match {
      case None => {
        logger.warn(s"Channel ${channelId} should be hosting a sign-up but does not exist")
        Future.successful(Nil)
      }
      case Some(m) => {
        for {
          gameStartMessage <- m
          users <- getSignups(api)
        } yield {
          gameStartMessage.getServer.toScala match {
            case None => {
              logger.warn(s"Could not identify server of ${gameStartMessage.getId}... continuing with default member names...")
              users.map { _.getName }
            }
            case Some(server) => {
              users.map { _.getDisplayName(server) }
            }
          }
        }
      }
    }

  def updateSignupList(api: DiscordApi): Future[Unit] =
    Future.successful(())
/*
    for {
      server <- getGameStartMessage(api)
      users <- getSignupNames(api).map { _.sorted }
    } yield {
      () /////
    }
 */

  override def onReactionsUpdated(message: Message): Unit = {
    updateSignupList(message.getApi)
  }

}

object SignupState {

  private val joinEmoji: String = Emoji.Clipboard

  private def getJoinReactions(message: Message)(using ExecutionContext): Future[collection.Seq[User]] =
    message.getReactionByEmoji(joinEmoji).toScala match {
      case None => Future.successful(Nil)
      case Some(reaction) => reaction.getUsers().asScala.map(_.asScala)
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
