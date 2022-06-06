
package com.mercerenies.werewolf
package manager

import id.Id
import util.TextDecorator.*
import logging.Logging
import command.{Command, CommandResponse}
import state.{GameState, SignupState}

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.Nameable
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.interaction.SlashCommandInteraction

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import scala.collection.mutable.HashMap
import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.concurrent.{ExecutionContext, Future}

final class GamesManager(
  val api: DiscordApi,
)(using ExecutionContext) extends Logging[GamesManager] {

  // Mapping from channel ID to game data
  private val games: HashMap[Id[TextChannel & Nameable], GameState] = HashMap()

  def createGame(channel: TextChannel & Nameable, host: User): Future[SignupState] = {
    SignupState.createGame(channel, host).map { state =>
      games(Id(channel)) = state
      state
    }
  }

  def hasGame(channelId: Id[TextChannel & Nameable]): Boolean =
    !getGame(channelId).isEmpty

  def getGame(channelId: Id[TextChannel & Nameable]): Option[GameState] =
    games.get(channelId)

  def updateGame(channelId: Id[TextChannel & Nameable], newState: GameState): Unit = {
    if (hasGame(channelId)) {
      games(channelId) = newState
    } else {
      // Warn and do nothing.
      logger.warn(s"Attempt to update state for nonexistent game in channel ${channelId} to ${newState}")
    }
  }

  private def withChannel[F[_]: Applicative](interaction: SlashCommandInteraction)(f: (TextChannel & Nameable) => F[CommandResponse[Unit]]): F[CommandResponse[Unit]] =
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

  private val newGameCommand: Command = Command.Term("new", "Host a new Werewolf game in the current channel") { interaction =>
    onNewGame(interaction).map { _.execute(interaction) }
  }

  private val startGameCommand: Command = Command.Term("start", "Start a game of Werewolf with the current player list") { interaction =>
    onStartGame(interaction).map { _.execute(interaction) }
  }

  val commands: List[Command] = List(newGameCommand, startGameCommand)

}

object GamesManager {

  def toNamed(channel: TextChannel): Option[TextChannel & Nameable] =
    channel match {
      case channel: Nameable => Some(channel)
      case _ => None
    }

}