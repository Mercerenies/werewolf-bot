
package com.mercerenies.werewolf
package state

import id.Id
import id.Ids.*
import util.TextDecorator.*
import util.Emoji
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

final class NightPhaseState(
  _gameProperties: GameProperties,
  private val players: List[User],
  private val board: Board,
)(
  using ExecutionContext,
) extends GameState(_gameProperties) {

  import NightPhaseState.logger
  import scalaz.EitherT.eitherTHoist

  private val playerIds: List[Id[User]] =
    players.map { Id(_) }

  override val listeningPlayerList: List[Id[User]] =
    playerIds

  private val playerNightHandlers: Map[Id[User], NightMessageHandler] =
    Map.from(board.playerRoleInstances.map { (k, v) => (k, v.nightHandler) })

  override def onReactionsUpdated(mgr: GamesManager, message: Message): Unit = {
    // No action
  }

  override def onMessageCreate(mgr: GamesManager, message: Message): Unit = {
    message.reply("It is currently nighttime. " + bold("Please do not post in this channel until day."))
  }

  override def onDirectMessageCreate(mgr: GamesManager, user: User, message: Message): Unit = {
    playerNightHandlers.get(Id(user)) match {
      case None => {
        // If this message occurs, NightPhaseState has misreported
        // listeningPlayerList and this is a bug.
        logger.warn(s"Trying to process DM from ${user} but I don't know what to do.")
      }
      case Some(nightHandler) => {
        val response = nightHandler.onDirectMessage(message.getContent)
        response.respondTo(mgr.api, message)
        ///// Test me
      }
    }
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("It is currently night in this game.").void)

}

object NightPhaseState extends Logging[RoleListState]
