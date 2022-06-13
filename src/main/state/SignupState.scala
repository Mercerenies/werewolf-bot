
package com.mercerenies.werewolf
package state

import id.{Id, Ids}
import id.Ids.*
import util.TextDecorator.*
import util.Emoji
import logging.Logging
import logging.Logs.warningToLogger
import name.{NameProvider, BaseNameProvider, DisplayNameProvider}
import command.{CommandResponse, Permissions}
import manager.GamesManager
import game.Rules
import properties.{GameProperties, DefaultGameProperties, DebugGameProperties}

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.ServerTextChannel
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

// A game in this state is accepting player sign-ups. A game begins in
// this state and will remain in this state until the host issues a
// `/wolfie start` command, at which point the game transitions into
// RoleListState.
final class SignupState(
  _gameProperties: GameProperties,
  private val gameStartMessageId: Id[Message],
  private val signupsMessageId: Id[Message],
)(
  using ExecutionContext,
) extends GameState(_gameProperties) {

  import SignupState.logger
  import scalaz.EitherT.eitherTHoist

  private def getGameStartMessage(api: DiscordApi): Future[Message] =
    api.getMessage(channelId, gameStartMessageId)

  private def getSignupsMessage(api: DiscordApi): Future[Message] =
    api.getMessage(channelId, signupsMessageId)

  def getSignups(api: DiscordApi): Future[collection.Seq[User]] =
    for {
      message <- getGameStartMessage(api)
      joinReactions <- SignupState.getJoinReactions(message)
    } yield {
      joinReactions.filter { user => !user.isBot }
    }

  private def getSignupNames(api: DiscordApi): Future[collection.Seq[String]] =
    for {
      message <- getGameStartMessage(api)
      server = api.getServerFromMessage(message)
      users <- getSignups(api)
    } yield {
      users.map { _.getDisplayName(server) }
    }

  def updateSignupList(api: DiscordApi): Future[Unit] =
    for {
      signupsMessage <- getSignupsMessage(api)
      users <- getSignupNames(api).map { _.sorted }
      _ <- signupsMessage.edit(SignupState.getSignupMessage(users)).asScala
    } yield {
      ()
    }

  override val listeningPlayerList: List[Id[User]] = Nil

  override def onReactionsUpdated(mgr: GamesManager, message: Message): Unit = {
    if (message.getId == gameStartMessageId.toLong) {
      updateSignupList(mgr.api)
    }
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    for {
      message <- getGameStartMessage(mgr.api)
      server = mgr.api.getServerFromMessage(message)
      signups <- getSignups(mgr.api)
    } yield {
      val user = interaction.getUser
      Permissions.mustBeAdminOrHost(server, hostId, user) {
        val playerCount = signups.length
        val rolesNeeded = Rules.rolesNeeded(playerCount)
        CommandResponse.simple(bold("Signups are now closed.") + s" There are ${playerCount} player(s). ${user.getMentionTag}, please ping me and indicate a list of ${rolesNeeded} roles to include in the game.").andThen { _ =>
          mgr.updateGame(channelId, RoleListState(gameProperties, signups.map(Id(_)).toList))
        }
      }
    }

  override def onStatusCommand(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    for {
      message <- getGameStartMessage(mgr.api)
    } yield {
      CommandResponse.simple("This Werewolf game is currently " + bold("accepting signups") + ". Sign up by replying to this post: " + message.getLink()).void
    }

}

object SignupState extends Logging[SignupState] {

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

  def createGame(channel: ServerTextChannel, host: User)(using ExecutionContext): Future[SignupState] = {
    val api = channel.getApi
    val text = gameStartMessage(host)
    val properties = DebugGameProperties( // DEBUG CODE
      channelId=Id(channel),
      hostId=Id(host),
    )
    for {
      startMessage <- channel.sendMessage(text).asScala
      signupsMessage <- channel.sendMessage("(Please wait)").asScala
      _ <- startMessage.addReaction(joinEmoji).asScala
      state = SignupState(properties, Id(startMessage), Id(signupsMessage))
      _ <- state.updateSignupList(api)
    } yield {
      state
    }
  }

}
