
package com.mercerenies.werewolf

import id.Id
import command.{Command, CommandResponse}
import state.{GameState, SignupState}

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.Nameable
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.interaction.SlashCommandInteraction

import scala.collection.mutable.HashMap
import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.concurrent.{ExecutionContext, Future}

final class GamesManager(using ExecutionContext) {

  // Mapping from channel ID to game data
  private val games: HashMap[Id[TextChannel & Nameable], GameState] = HashMap()

  def createGame(channel: TextChannel & Nameable, host: User): Future[SignupState] = {
    SignupState.createGame(channel, host).map { state =>
      games(Id(channel)) = state
      state
    }
  }

  def getGame(channelId: Id[TextChannel & Nameable]): Option[GameState] =
    games.get(channelId)

  private def onStartGame(interaction: SlashCommandInteraction): CommandResponse[Unit] =
    interaction.getChannel.toScala match {
      case None => CommandResponse.ephemeral("Please use this command in a server channel.")
      case Some(channel) => {
        GamesManager.toNamed(channel) match {
          case None => {
            CommandResponse.ephemeral("Sorry, you can't play Werewolf in a DM. Please issue that command in the channel you want to start the game in.")
          }
          case Some(channel) => {
            if (getGame(Id(channel)).isEmpty) {
              CommandResponse(s"Starting a game of One Night Ultimate Werewolf in ${channel.getName}.", Nil) { _ =>
                createGame(channel, interaction.getUser)
                ()
              }
            } else {
              CommandResponse.ephemeral("There is already a game running in this channel.")
            }
          }
        }
      }
    }

  private val startGameCommand: Command = Command.Term("new", "Start a new Werewolf game") { interaction =>
    onStartGame(interaction).execute(interaction)
  }

  val commands: List[Command] = List(startGameCommand)

}

object GamesManager {

  def toNamed(channel: TextChannel): Option[TextChannel & Nameable] =
    channel match {
      case channel: Nameable => Some(channel)
      case _ => None
    }

}
