
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
  _playerOrder: PlayerOrder,
  _initialBoard: Board,
)(
  using ExecutionContext,
) extends NighttimePhaseState(_gameProperties, _playerOrder, _initialBoard) {

  import NightPhaseState.logger
  import scalaz.EitherT.eitherTHoist

  override val phase = NightPhase.Night

  override def nextState(result: NightPhaseResult): GameState =
    DayPhaseState(gameProperties, playerOrder, result.board, result.history, result.revealedCards)

}

object NightPhaseState extends Logging[NightPhaseState]
