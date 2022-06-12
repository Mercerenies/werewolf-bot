
package com.mercerenies.werewolf
package state

import id.{Id, UserMapping}
import id.Ids.*
import timer.Cancellable
import util.TextDecorator.*
import util.{Emoji, Cell}
import logging.Logging
import logging.Logs.warningToLogger
import name.{NameProvider, BaseNameProvider, DisplayNameProvider}
import command.CommandResponse
import manager.GamesManager
import game.Rules
import game.board.Board
import game.role.Role
import game.parser.ListParser
import game.night.NightMessageHandler
import game.response.FeedbackMessage
import properties.GameProperties

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

final class DayPhaseState(
  _gameProperties: GameProperties,
  private val playerIds: List[Id[User]],
  private val board: Board,
)(
  using ExecutionContext,
) extends GameState(_gameProperties) {

  import DayPhaseState.logger

  override val listeningPlayerList: List[Id[User]] =
    Nil

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("There is already a game in this channel.").void)

  override def onEnterState(mgr: GamesManager): Unit = {
    val timer = mgr.timer
    val channel = mgr.api.getServerTextChannel(channelId)
    val server = channel.getServer
    dayStartMessage(mgr.api, server).flatMap { channel.sendMessage(_).asScala }
    ////
  }

  override def onExitState(mgr: GamesManager): Unit = {
    ////
  }

  private def dayStartMessage(api: DiscordApi, server: Server): Future[String] =
    for {
      players <- playerIds.traverse { api.getUser(_) }
    } yield {
      bold("It is now daytime!") + "\n\n" +
        "The following players are participating: " + players.map(_.getDisplayName(server)).mkString(", ") + "\n" + // TODO Correct order
        "The following roles are in play: " + board.roles.mkString(", ") + "\n" +
        bold(s"Day will end in ${gameProperties.dayPhaseLength}")
    }

}

object DayPhaseState extends Logging[DayPhaseState]