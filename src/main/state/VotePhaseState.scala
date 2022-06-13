
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
import game.board.{Board, AssignmentBoard, AssignmentBoardFormatter, StandardAssignmentBoardFormatter}
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
import scala.collection.concurrent.TrieMap

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

final class VotePhaseState(
  _gameProperties: GameProperties,
  override val playerIds: List[Id[User]],
  private val board: Board,
)(
  using ExecutionContext,
) extends GameState(_gameProperties) with SchedulingState with WithNamedUserList {

  import VotePhaseState.logger

  private val playerVotes: TrieMap[Id[User], Id[User]] = TrieMap()

  override val listeningPlayerList: List[Id[User]] =
    Nil

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("There is already a game in this channel.").void)

  override def onStatusCommand(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful {
      CommandResponse.simple("This Werewolf game is currently " + bold("in the voting phase") + ". Please sent your vote to me via DM.").void
    }

  override def onEnterState(mgr: GamesManager): Unit = {
    val channel = mgr.api.getServerTextChannel(channelId)
    val server = channel.getServer
    voteStartMessage(mgr.api, server).flatMap { channel.sendMessage(_).asScala }
    ////
  }

  private def voteStartMessage(api: DiscordApi, server: Server): Future[String] =
    for {
      players <- playerIds.traverse { api.getUser(_) }
    } yield {
      bold("It is now time to vote!") + "\n\n" +
        "As a reminder, the following players are participating: " + players.map(_.getDisplayName(server)).mkString(", ") + "\n" + // TODO Correct order
      "Please " + bold("DM me your vote") + ". All players must submit a vote.\n" +
      bold(s"The voting phase will end in ${gameProperties.votePhaseLength}")
    }

}

object VotePhaseState extends Logging[VotePhaseState]
