
package com.mercerenies.werewolf

import id.Id
import command.Command
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

  private def onStartGame(interaction: SlashCommandInteraction): Unit = {
    interaction.getChannel.toScala.foreach { channel =>
      GamesManager.toNamed(channel) match {
        case None => {
          interaction.createImmediateResponder()
            .setContent("Sorry, you can't play Werewolf in a DM. Please issue that command in the channel you want to start the game in.")
            .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
            .respond()
        }
        case Some(channel) => {
          if (getGame(Id(channel)).isEmpty) {
            interaction.createImmediateResponder()
              .setContent(s"Starting a game of One Night Ultimate Werewolf in ${channel.getName}.")
              .respond()
              .asScala.foreach { _ =>
                createGame(channel, interaction.getUser)
              }
          } else {
            interaction.createImmediateResponder()
              .setContent("There is already a game running in this channel.")
              .setFlags(InteractionCallbackDataFlag.EPHEMERAL)
              .respond()
          }
        }
      }
    }
  }

  private val startGameCommand: Command = Command.Term("new", "Start a new Werewolf game")(onStartGame)

  val commands: List[Command] = List(startGameCommand)

}

object GamesManager {

  def toNamed(channel: TextChannel): Option[TextChannel & Nameable] =
    channel match {
      case channel: Nameable => Some(channel)
      case _ => None
    }

}
