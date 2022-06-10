
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

final class NightPhaseState(
  _gameProperties: GameProperties,
  private val playerIds: List[Id[User]],
  private val board: Board,
)(
  using ExecutionContext,
) extends GameState(_gameProperties) with SchedulingState {

  import NightPhaseState.logger
  import scalaz.EitherT.eitherTHoist

  override val listeningPlayerList: List[Id[User]] =
    playerIds

  private val playerNightHandlers: Map[Id[User], NightMessageHandler] =
    Map.from(board.playerRoleInstances.map { (k, v) => (k, v.nightHandler) })

  override def onDirectMessageCreate(mgr: GamesManager, user: User, message: Message): Unit = {
    playerNightHandlers.get(Id(user)) match {
      case None => {
        // If this message occurs, NightPhaseState has misreported
        // listeningPlayerList and this is a bug.
        logger.error(s"Trying to process DM from ${user} but I don't know what to do.")
      }
      case Some(nightHandler) => {
        val response = nightHandler.onDirectMessage(message.getContent)
        response.respondTo(mgr.api, message)
      }
    }
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("It is currently night in this game.").void)

  override def onEnterState(mgr: GamesManager): Unit = {
    val timer = mgr.timer

    // Schedule midnight reminder
    gameProperties.nightPhaseReminderTime.foreach { time =>
      schedule(mgr, time.toDuration) { () =>
        sendNightReminder(mgr)
      }
    }

    // Schedule end of night phase
    schedule(mgr, gameProperties.nightPhaseLength.toDuration) { () =>
      endOfNight(mgr)
    }

  }

  private def sendNightReminder(mgr: GamesManager): Unit = {
    for {
      _ <- Future.traverse(playerNightHandlers) { (playerId, handler) =>
        mgr.api.getUser(playerId).flatMap { NightPhaseState.sendNightReminderTo(mgr.api, _, handler) }
      }
    } yield {
      ()
    }
  }

  private def endOfNight(mgr: GamesManager): Unit = {
    val channel = mgr.api.getServerTextChannel(channelId)
    for {
      userMapping <- UserMapping.fromServer(mgr.api, channel.getServer, playerIds)
    } yield {
      val (finalBoard, nightMessagesFuture) = NightPhaseState.evaluateNightPhase(userMapping, board)
      nightMessagesFuture.foreach { _ =>
        val newState = DayPhaseState(gameProperties, playerIds, board)
        mgr.updateGame(channelId, newState)
      }
    }
  }

}

object NightPhaseState extends Logging[NightPhaseState] {

  private def sendNightReminderTo(api: DiscordApi, player: User, handler: NightMessageHandler)(using ExecutionContext): Future[Unit] = {
    handler.midnightReminder match {
      case None =>
        Future.successful(())
      case Some(message) =>
        player.sendMessage(message).asScala.map { _ => () }
    }
  }

  // The returned future is successful when all night DMs have been
  // sent. The final board state does not depend on this, but it may
  // be worth waiting until this completes to start the actual day
  // phase.
  def evaluateNightPhase(mapping: UserMapping, board: Board)(using ExecutionContext): (Board, Future[Unit]) = {
    val instances = board.playerRoleInstances.sortBy { (_, roleInstance) => - roleInstance.role.precedence }
    val stateMonad: State[Board, List[(Id[User], FeedbackMessage)]] = instances.traverse { (userId, roleInstance) =>
      roleInstance.nightAction(mapping, userId).map { (userId, _) }
    }
    // So Scala dies a violent and bloody death if I don't include the
    // 'using' argument here. No idea why, but probably related to
    // https://github.com/lampepfl/dotty/issues/12479.
    val (finalBoard, messages) = stateMonad(board)(using scalaz.Id.id)
    val messagesFuture = messages.traverse { (userId, feedback) => feedback.sendTo(mapping(userId)) }.void
    (finalBoard, messagesFuture)
  }

}
