
package com.mercerenies.werewolf

import id.Id
import logging.Logging
import command.{Command, CommandResponse}
import state.{GameState, SignupState}

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.Nameable
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.interaction.SlashCommandInteraction

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

  private def onNewGame(interaction: SlashCommandInteraction): CommandResponse[Unit] =
    interaction.getChannel.toScala match {
      case None => CommandResponse.ephemeral("Please use this command in a server channel.").void
      case Some(channel) => {
        GamesManager.toNamed(channel) match {
          case None => {
            CommandResponse.ephemeral("Sorry, you can't play Werewolf in a DM. Please issue that command in the channel you want to start the game in.").void
          }
          case Some(channel) => {
            if (getGame(Id(channel)).isEmpty) {
              CommandResponse(s"Starting a game of One Night Ultimate Werewolf in ${channel.getName}.", Nil) { _ =>
                createGame(channel, interaction.getUser)
                ()
              }
            } else {
              CommandResponse.ephemeral("There is already a game running in this channel.").void
            }
          }
        }
      }
    }

  private val newGameCommand: Command = Command.Term("new", "Host a new Werewolf game in the current channel") { interaction =>
    onNewGame(interaction).execute(interaction)
  }

  val commands: List[Command] = List(newGameCommand)

}

object GamesManager {

  def toNamed(channel: TextChannel): Option[TextChannel & Nameable] =
    channel match {
      case channel: Nameable => Some(channel)
      case _ => None
    }

}
