
package com.mercerenies.werewolf
package manager

import id.Id
import util.TextDecorator.*
import logging.Logging
import logging.Logs.*
import command.{Command, CommandResponse}
import state.{GameState, SignupState}
import timer.Timer

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.{ServerTextChannel, TextChannel}
import org.javacord.api.entity.user.User
import org.javacord.api.entity.Nameable
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.interaction.SlashCommandInteraction

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import scala.util.Try
import scala.collection.concurrent.TrieMap
import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.concurrent.{ExecutionContext, Future}

final class GamesManager(
  val api: DiscordApi,
)(using ExecutionContext) {

  import GamesManager.logger

  // Mapping from channel ID to game data
  private val games: TrieMap[Id[ServerTextChannel], GameState] = TrieMap()

  // Mapping from user ID to game state(s) of interest
  private val users: TrieMap[Id[User], List[GameState]] = TrieMap()

  val timer: Timer = Timer()

  private def removeUsersFor(state: GameState): Unit = {
    state.listeningPlayerList.foreach { userId =>
      users.updateWith(userId) {
        case None => None
        case Some(list) => {
          val newList = list.filter { _ != state }
          if (newList.isEmpty) { None } else { Some(newList) }
        }
      }
    }
  }

  private def addUsersFor(state: GameState): Unit = {
    state.listeningPlayerList.foreach { userId =>
      users.updateWith(userId) {
        case None => Some(List(state))
        case Some(list) => Some(state :: users(userId))
      }
    }
  }

  def createGame(channel: ServerTextChannel, host: User): Future[SignupState] = {
    logger.info(s"Creating new game in channel ${channel.getId} with host ${host.getName}")
    SignupState.createGame(channel, host).map { state =>
      games(Id(channel)) = state
      addUsersFor(state)
      state.onEnterState(this)
      state
    }
  }

  def hasGame(channelId: Id[ServerTextChannel]): Boolean =
    !getGame(channelId).isEmpty

  def getGame(channelId: Id[ServerTextChannel]): Option[GameState] =
    games.get(channelId)

  def getGamesForUser(userId: Id[User]): List[GameState] =
    users.getOrElse(userId, Nil)

  def endGame(channelId: Id[ServerTextChannel]): Unit = {
    games.remove(channelId)
  }

  def updateGame(channelId: Id[ServerTextChannel], newState: GameState): Unit = {
    if (hasGame(channelId)) {
      logger.info(s"Updating game in channel ${channelId}, game is now in state ${newState}")
      games(channelId).onExitState(this)
      removeUsersFor(games(channelId))
      games(channelId) = newState
      addUsersFor(newState)
      newState.onEnterState(this)
    } else {
      // Warn and do nothing.
      logger.warn(s"Attempt to update state for nonexistent game in channel ${channelId} to ${newState}")
    }
  }

  private def withChannel[F[_]: Applicative](interaction: SlashCommandInteraction)(f: (ServerTextChannel) => F[CommandResponse[Unit]]): F[CommandResponse[Unit]] =
    interaction.getChannel.toScala match {
      case None => CommandResponse.ephemeral("Please use this command in a server channel.").void.point
      case Some(channel) => {
        GamesManager.toNamed(channel) match {
          case None => {
            CommandResponse.ephemeral("Sorry, you can't use Werewolf commands in a DM. Please use that command in the server channel of the game.").void.point
          }
          case Some(channel) => {
            f(channel)
          }
        }
      }
    }

  private def withState[F[_]: Applicative](interaction: SlashCommandInteraction)(f: (GameState) => F[CommandResponse[Unit]]): F[CommandResponse[Unit]] =
    withChannel(interaction) { channel =>
      getGame(Id(channel)) match {
        case None => {
          CommandResponse.ephemeral("There is no game running in this channel. If you want to host a new game, use " + code("/werewolf new") + ".").void.point
        }
        case Some(state) => {
          f(state)
        }
      }
    }

  // Note: onNewGame doesn't use its Future (it's always
  // Future.successful), but the other commands do, so this one gets a
  // Future for consistency with them.
  private def onNewGame(interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    withChannel(interaction) { channel =>
      if (getGame(Id(channel)).isEmpty) {
        CommandResponse(s"Starting a game of One Night Ultimate Werewolf in ${channel.getName}.", Nil) { _ =>
          createGame(channel, interaction.getUser)
          ()
        }.point
      } else {
        CommandResponse.ephemeral("There is already a game running in this channel.").void.point
      }
    }

  private def onStartGame(interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    withState(interaction) { state =>
      state.onStartGame(this, interaction)
    }

  private def onStatusCommand(interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    withState(interaction) { state =>
      state.onStatusCommand(this, interaction)
    }

  private val newGameCommand: Command = Command.Term("new", "Host a new Werewolf game in the current channel") { interaction =>
    Try {
      onNewGame(interaction).map { _.execute(interaction) }
    }.logErrors(logger)
  }

  private val startGameCommand: Command = Command.Term("start", "Start a game of Werewolf with the current player list") { interaction =>
    Try {
      onStartGame(interaction).map { _.execute(interaction) }
    }.logErrors(logger)
  }

  private val statusCommand: Command = Command.Term("status", "Get the status of the current game in the channel") { interaction =>
    Try {
      onStatusCommand(interaction).map { _.execute(interaction) }
    }.logErrors(logger)
  }

  val commands: List[Command] = List(newGameCommand, startGameCommand, statusCommand)

}

object GamesManager extends Logging[GamesManager] {

  def toNamed(channel: TextChannel): Option[ServerTextChannel] =
    channel match {
      case channel: ServerTextChannel => Some(channel)
      case _ => None
    }

}
