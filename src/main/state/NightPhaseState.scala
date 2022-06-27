
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
import game.{Rules, NightPhaseEvaluator, NightPhaseResult}
import game.board.Board
import game.role.Role
import game.parser.ListParser
import game.night.NightMessageHandler
import game.response.FeedbackMessage
import game.record.{RecordedGameHistory, SnapshotRecord}
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
  override val playerIds: List[Id[User]],
  private val board: Board,
)(
  using ExecutionContext,
) extends GameState(_gameProperties) with SchedulingState with WithUserMapping {

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

  private def initialHistory: RecordedGameHistory =
    RecordedGameHistory(
      SnapshotRecord(board.toSnapshot(playerIds)),
    )

  private def endOfNight(mgr: GamesManager): Unit = {
    for {
      userMapping <- getUserMapping(mgr.api)
    } yield {
      val (finalBoard, history, nightMessagesFuture) = NightPhaseState.evaluateNightPhaseAndSend(initialHistory, userMapping, playerIds, board)
      nightMessagesFuture.foreach { _ =>
        val newState = DayPhaseState(gameProperties, playerIds, board, history)
        mgr.updateGame(channelId, newState)
      }
    }
  }

  override def onStatusCommand(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful {
      CommandResponse.simple("This Werewolf game is currently " + bold("in the night phase") + ".").void
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
  def evaluateNightPhaseAndSend(
    initialHistory: RecordedGameHistory,
    mapping: UserMapping,
    ids: List[Id[User]],
    board: Board,
  )(
    using ExecutionContext,
  ): (Board, RecordedGameHistory, Future[Unit]) = {
    // TODO When there are multiple night phases, we'll need to keep
    // the old history here
    val result = NightPhaseEvaluator.evaluate(board, ids, initialHistory)
    val messagesFuture = result.feedback.toList.traverse { (userId, feedback) =>
      feedback.sendTo(mapping(userId))
    }.void
    (result.board, result.history, messagesFuture)
  }

}
