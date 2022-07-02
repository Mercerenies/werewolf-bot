
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

// The common superclass to NightPhaseState and DuskPhaseState.
abstract class NighttimePhaseState(
  _gameProperties: GameProperties,
  override val playerOrder: PlayerOrder,
  val initialBoard: Board,
)(
  using ExecutionContext,
) extends GameState(_gameProperties) with SchedulingState with WithUserMapping {

  import NighttimePhaseState.logger
  import scalaz.EitherT.eitherTHoist

  def phase: NightPhase

  def nextState(result: NightPhaseResult): GameState

  def initialHistory: RecordedGameHistory

  def startMessage: String

  override val listeningPlayerList: List[Id[User]] =
    playerOrder.toList

  private val playerHandlers: Cell[Map[Id[User], NightMessageHandler]] =
    Cell(Map())

  override def onDirectMessageCreate(mgr: GamesManager, user: User, message: Message): Unit = {
    playerHandlers.value.get(Id(user)) match {
      case None => {
        // If this message occurs, NighttimePhaseState has misreported
        // listeningPlayerList and this is a bug.
        logger.error(s"Trying to process DM from ${user} but I don't know what to do.")
      }
      case Some(handler) => {
        val response = handler.onDirectMessage(message.getContent)
        response.respondTo(mgr.api, message)
      }
    }
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral(s"It is currently ${bold(phase.toString.toLowerCase)} in this game.").void)

  override def onEnterState(mgr: GamesManager): Unit = {

    val channel = mgr.api.getNamedTextChannel(channelId)

    for {
      userMapping <- getUserMapping(mgr.api)
      _ <- channel.sendMessage(startMessage).asScala
    } {
      // Setup night handlers
      this.playerHandlers.value = Map.from(initialBoard.playerRoleInstances.map { (k, v) =>
        (k, phase.getHandler(v))
      })
      initialBoard.playerRoleInstances.toList.traverse { (userId, roleInstance) =>
        for {
          user <- mgr.api.getUser(userId)
          username = userMapping.nameOf(userId)
          _ <- user.sendMessage(phase.getHandler(roleInstance).initialNightMessage).asScala
        } yield {
          ()
        }
      }

      // Schedule midnight reminder
      gameProperties.nighttimeReminderTime(phase).foreach { time =>
        schedule(mgr, time.toDuration) { () =>
          sendNightReminder(mgr)
        }
      }

      // Schedule end of night phase
      schedule(mgr, gameProperties.nighttimeLength(phase).toDuration) { () =>
        endOfNight(mgr)
      }

    }

  }

  private def sendNightReminder(mgr: GamesManager): Unit = {
    for {
      _ <- Future.traverse(playerHandlers.value) { (playerId, handler) =>
        mgr.api.getUser(playerId).flatMap { NighttimePhaseState.sendNightReminderTo(mgr.api, _, handler) }
      }
    } yield {
      ()
    }
  }

  def endOfNight(mgr: GamesManager): Unit = {
    for {
      userMapping <- getUserMapping(mgr.api)
      result <- NighttimePhaseState.evaluateAndSend(initialHistory, userMapping, playerOrder, initialBoard, phase)
    } yield {
      mgr.updateGame(channelId, nextState(result))
    }
  }

  override def onStatusCommand(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful {
      CommandResponse.simple("This Werewolf game is currently " + bold(s"in the ${phase.toString.toLowerCase} phase") + ".").void
    }

}

object NighttimePhaseState extends Logging[NighttimePhaseState] {

  private def sendNightReminderTo(api: DiscordApi, player: User, handler: NightMessageHandler)(using ExecutionContext): Future[Unit] = {
    handler.midnightReminder match {
      case None =>
        Future.successful(())
      case Some(message) =>
        player.sendMessage(message).asScala.map { _ => () }
    }
  }

  def evaluateAndSend(
    initialHistory: RecordedGameHistory,
    mapping: UserMapping,
    order: PlayerOrder,
    board: Board,
    phase: NightPhase,
  )(
    using ExecutionContext,
  ): Future[NightPhaseResult] = {
    val result = NightPhaseEvaluator.evaluate(board, phase, order, initialHistory)
    result.sendFeedback(mapping) >| result
  }

}
