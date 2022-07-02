
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
import game.{Rules, NightPhaseEvaluator, NightPhase, NightPhaseResult}
import game.board.{Board, Position, PlayerOrder}
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
  override val playerOrder: PlayerOrder,
  initialBoard: Board,
)(
  using ExecutionContext,
) extends GameState(_gameProperties) with SchedulingState with WithUserMapping {

  import NightPhaseState.logger
  import scalaz.EitherT.eitherTHoist

  override val listeningPlayerList: List[Id[User]] =
    playerOrder.toList

  private val playerNightHandlers: Cell[Map[Id[User], NightMessageHandler]] =
    Cell(Map())

  private val initialGameHistory: Cell[RecordedGameHistory] =
    Cell(
      RecordedGameHistory(SnapshotRecord(initialBoard.toSnapshot(playerOrder))),
    )

  private val boardCell: Cell[Board] =
    Cell(initialBoard)

  override def onDirectMessageCreate(mgr: GamesManager, user: User, message: Message): Unit = {
    playerNightHandlers.value.get(Id(user)) match {
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

    for {
      userMapping <- getUserMapping(mgr.api)
      // Perform dusk actions
      result <- NightPhaseState.evaluateDuskPhaseAndSend(initialGameHistory.value, userMapping, playerOrder, boardCell.value)
    } {
      initialGameHistory.modify { _ ++ result.history }
      boardCell.value = result.board

      // Setup night handlers
      this.playerNightHandlers.value = Map.from(boardCell.value.playerRoleInstances.map { (k, v) => (k, v.nightHandler) })
      result.board.playerRoleInstances.toList.traverse { (userId, roleInstance) =>
        for {
          user <- mgr.api.getUser(userId)
          username = userMapping.nameOf(userId)
          _ <- user.sendMessage(roleInstance.nightHandler.initialNightMessage).asScala
        } yield {
          ()
        }
      }

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

  }

  private def sendNightReminder(mgr: GamesManager): Unit = {
    for {
      _ <- Future.traverse(playerNightHandlers.value) { (playerId, handler) =>
        mgr.api.getUser(playerId).flatMap { NightPhaseState.sendNightReminderTo(mgr.api, _, handler) }
      }
    } yield {
      ()
    }
  }

  private def endOfNight(mgr: GamesManager): Unit = {
    for {
      userMapping <- getUserMapping(mgr.api)
      result <- NightPhaseState.evaluateNightPhaseAndSend(initialGameHistory.value, userMapping, playerOrder, boardCell.value)
    } yield {
      val newState = DayPhaseState(gameProperties, playerOrder, result.board, result.history, result.revealedCards)
      mgr.updateGame(channelId, newState)
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

  def evaluateDuskPhaseAndSend(
    initialHistory: RecordedGameHistory,
    mapping: UserMapping,
    order: PlayerOrder,
    board: Board,
  )(
    using ExecutionContext,
  ): Future[NightPhaseResult] = {
    val result = NightPhaseEvaluator.evaluate(board, NightPhase.Dusk, order, initialHistory)
    result.sendFeedback(mapping) >| result
  }

  def evaluateNightPhaseAndSend(
    initialHistory: RecordedGameHistory,
    mapping: UserMapping,
    order: PlayerOrder,
    board: Board,
  )(
    using ExecutionContext,
  ): Future[NightPhaseResult] = {
    val result = NightPhaseEvaluator.evaluate(board, NightPhase.Night, order, initialHistory)
    result.sendFeedback(mapping) >| result
  }

}
