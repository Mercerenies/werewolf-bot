
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
import game.board.{Board, Position, PlayerOrder}
import game.board.assignment.{AssignmentBoard, AssignmentBoardFormatter, StandardAssignmentBoardFormatter}
import game.role.Role
import game.parser.ListParser
import game.night.NightMessageHandler
import game.response.FeedbackMessage
import game.record.RecordedGameHistory
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
  override val playerOrder: PlayerOrder,
  override val board: Board,
  private val initialHistory: RecordedGameHistory,
  _revealedCards: Set[Position],
)(
  using ExecutionContext,
) extends GameState(_gameProperties) with SchedulingState with WithUserMapping with WithAssignmentParser {

  import DayPhaseState.logger

  override val listeningPlayerList: List[Id[User]] =
    Nil

  private val assignmentBoard: Cell[AssignmentBoard] =
    Cell(AssignmentBoard.empty(board, playerOrder))

  private val revealedCards: List[Position] =
    _revealedCards.toList.sorted(using Position.ordering(playerOrder))

  // Note: There aren't a lot of roles that will add events to this
  // during the day phase, but a few might need to make notes. In
  // particular, the Curator's artifact may have some things to say if
  // its speech restriction is not obeyed.
  private val history: Cell[RecordedGameHistory] =
    Cell(initialHistory)

  override def onMessageCreate(mgr: GamesManager, message: Message): Unit = {
    val text = message.getContent
    for {
      parser <- getAssignmentParser(mgr.api)
      formatter <- getBoardFormatter(mgr.api)
    } {
      val assignments = parser.parseFullMessage(text)
      // If we got any results, then run them and print the result. If
      // not, then assume the message wasn't directed at us and simply
      // ignore it completely.
      if (!assignments.isEmpty) {
        val fn = assignments.foldLeft(identity[AssignmentBoard]) { _ andThen _ }
        assignmentBoard.lock {
          assignmentBoard.modify(fn)
          message.reply(formatter.format(assignmentBoard.value))
        }
      }
    }
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("There is already a game in this channel.").void)

  override def onStatusCommand(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    for {
      formatter <- getBoardFormatter(mgr.api)
    } yield {
      CommandResponse.simple(formatter.format(assignmentBoard.value)).void
    }

  override def onEnterState(mgr: GamesManager): Unit = {
    val channel = mgr.api.getServerTextChannel(channelId)
    val server = channel.getServer

    // Send out day start messages
    for {
      // Generic day start message
      userMapping <- getUserMapping(mgr.api)
      dayStart <- dayStartMessage(mgr.api, server)
      _ <- channel.sendMessage(dayStart).asScala
      // Revealed cards
      _ <- util.foldM(revealedCards, ()) { (_, pos) =>
        val role = board(pos).role
        val message = pos match {
          case Position.Table(tablePos) => s"The ${bold(tablePos.name)} card has been flipped face up: It is ${bold(role.name)}."
          case Position.Player(id) => s"The card in front of ${bold(userMapping.nameOf(id))} card has been flipped face up: It is ${bold(role.name)}."
        }
        channel.sendMessage(message).asScala >| ()
      }
    } ()

    // Schedule end of day phase
    schedule(mgr, gameProperties.dayPhaseLength.toDuration) { () =>
      endOfDay(mgr)
    }

  }

  private def dayStartMessage(api: DiscordApi, server: Server): Future[String] =
    for {
      players <- playerOrder.toList.traverse { api.getUser(_) }
    } yield {
      bold("It is now daytime!") + "\n\n" +
        "The following players are participating (in table order): " + players.map(_.getDisplayName(server)).mkString(", ") + "\n" +
        "The following roles are in play: " + board.roles.mkString(", ") + "\n" +
        bold(s"Day will end in ${gameProperties.dayPhaseLength}")
    }

  private def getBoardFormatter(api: DiscordApi): Future[AssignmentBoardFormatter] =
    for {
      userMapping <- getUserMapping(api)
    } yield {
      StandardAssignmentBoardFormatter(userMapping)
    }

  private def endOfDay(mgr: GamesManager): Unit = {
    val finalHistory = history.value
    val newState = VotePhaseState(gameProperties, playerOrder, board, finalHistory)
    mgr.updateGame(channelId, newState)
  }

}

object DayPhaseState extends Logging[DayPhaseState]
