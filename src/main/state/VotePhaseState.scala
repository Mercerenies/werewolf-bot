
package com.mercerenies.werewolf
package state

import id.{Id, UserMapping}
import id.Ids.*
import timer.Cancellable
import util.TextDecorator.*
import util.RandomUtil.*
import util.{Emoji, Cell, Grammar}
import logging.Logging
import logging.Logs.warningToLogger
import name.{NameProvider, BaseNameProvider, DisplayNameProvider}
import command.CommandResponse
import manager.GamesManager
import game.Rules
import game.board.{Board, Endgame, PlayerOrder}
import game.board.assignment.{AssignmentBoard, AssignmentBoardFormatter, StandardAssignmentBoardFormatter}
import game.role.Role
import game.role.wincon.WinCondition
import game.parser.ListParser
import game.night.NightMessageHandler
import game.response.FeedbackMessage
import game.record.{RecordedGameHistory, PlayerVotesRecord, PlayerWinRecord, PlayerDeathsRecord}
import game.votes.{Votals, VotingEvaluator}
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
import scala.util.Random

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

final class VotePhaseState(
  _gameProperties: GameProperties,
  override val playerOrder: PlayerOrder,
  private val board: Board,
  private val history: RecordedGameHistory,
)(
  using ExecutionContext,
) extends GameState(_gameProperties) with SchedulingState with WithUserMapping with WithNamedUserList {

  import VotePhaseState.logger

  private val playerVotes: TrieMap[Id[User], Id[User]] = TrieMap()

  override val listeningPlayerList: List[Id[User]] =
    playerOrder.toList

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("There is already a game in this channel.").void)

  override def onStatusCommand(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful {
      CommandResponse.simple("This Werewolf game is currently " + bold("in the voting phase") + ". Please send your vote to me via DM.").void
    }

  override def onDirectMessageCreate(mgr: GamesManager, user: User, message: Message): Unit = {
    val text = message.getContent
    for {
      matcher <- getUserListMatcher(mgr.api)
    } {
      val matches = matcher.findAll(text).toList
      matches.length match {
        case 0 => {
          message.reply("Please indicate the name of a player you wish to vote for.")
        }
        case 1 if (matches(0).id == Id(user)) && (!gameProperties.isSelfVotingAllowed) => {
          message.reply("You cannot vote for yourself.")
        }
        case 1 => {
          playerVotes(Id(user)) = matches(0).id
          message.reply("Voting for " + bold(matches(0).name))
        }
        case _ => {
          message.reply("Please indicate the name of only one player. You can't vote for multiple.")
        }
      }
    }
  }

  override def onEnterState(mgr: GamesManager): Unit = {
    val channel = mgr.api.getServerTextChannel(channelId)
    val server = channel.getServer

    // Send initial message in public
    voteStartMessage(mgr.api, server).flatMap { channel.sendMessage(_).asScala }

    // Send message to all players
    for {
      userList <- getUserList(mgr.api)
      _ <- playerOrder.toList.traverse { playerId =>
        val filteredList = if (!gameProperties.isSelfVotingAllowed) {
          userList.filter { _.id != playerId }
        } else {
          userList
        }
        val userNames = filteredList.map(_.name)
        val directMessage = bold("Please indicate the name of the player you wish to vote for.") + " Options are " + Grammar.conjunctionList(userNames) + "."
        mgr.api.getUser(playerId) >>= { _.sendMessage(directMessage).asScala }
      }
    } {
      ()
    }

    // Schedule end of voting phase
    schedule(mgr, gameProperties.votePhaseLength.toDuration) { () =>
      endOfVotes(mgr)
    }

  }

  private def voteStartMessage(api: DiscordApi, server: Server): Future[String] =
    for {
      players <- playerOrder.toList.traverse { api.getUser(_) }
    } yield {
      bold("It is now time to vote!") + "\n\n" +
        "As a reminder, the following players are participating: " + players.map(_.getDisplayName(server)).mkString(", ") + "\n" +
      "Please " + bold("DM me your vote") + ". All players must submit a vote.\n" +
      bold(s"The voting phase will end in ${gameProperties.votePhaseLength}")
    }

  private def compileVotes(api: DiscordApi): Future[Votals[Id[User]]] = {
    val server = api.getServerTextChannel(channelId).getServer
    val originalMap: Map[Id[User], Id[User]] = Map.from(playerVotes) // Shallow copy
    util.foldM(playerOrder.toList, originalMap) { (map, id) =>
      // For any player who failed to vote, choose a random vote
      // target and inform them that this has been done.
      for {
        newTarget <- map.get(id) match {
          case None => {
            val validTargets = if (gameProperties.isSelfVotingAllowed) {
              playerOrder.toList
            } else {
              playerOrder.toList.filter { _ != id }
            }
            val randomTarget = Random.sample(validTargets)
            for {
              srcUser <- api.getUser(id)
              targetUser <- api.getUser(randomTarget)
              _ <- srcUser.sendMessage("The voting phase is over. Your vote has been randomly assigned to " + bold(targetUser.getDisplayName(server))).asScala
            } yield {
              randomTarget
            }
          }
          case Some(x) => {
            Future.successful(x)
          }
        }
      } yield {
        originalMap + ((id, newTarget))
      }
    } map { Votals(_) }
  }

  private def endOfVotes(mgr: GamesManager): Unit = {
    val channel = mgr.api.getServerTextChannel(channelId)
    for {
      userMapping <- getUserMapping(mgr.api)
      votes <- compileVotes(mgr.api)
      (deathRoster, deathRecords) = VotingEvaluator.evaluate(board, playerOrder, votes)
      _ <- channel.sendMessage(bold("The game is now over.")).asScala
      _ <- channel.sendMessage(VotePhaseState.deathMessage(userMapping, deathRoster.dead)).asScala
      endgame = Endgame(board, playerOrder, deathRoster.dead)
      winnerIds = WinCondition.determineWinners(endgame).toList
      finalHistory = history ++ List(PlayerVotesRecord(votes)) ++ deathRecords ++ List(PlayerDeathsRecord(deathRoster.dead), PlayerWinRecord(winnerIds))
      _ <- channel.sendMessage(VotePhaseState.winMessage(userMapping, winnerIds)).asScala
      recordExporter = gameProperties.recordExporter(mgr.api)
      _ <- recordExporter.exportRecord(finalHistory, userMapping)
    } {
      mgr.endGame(channelId)
    }
  }

}

object VotePhaseState extends Logging[VotePhaseState] {

  private def deathMessage(mapping: UserMapping, deaths: List[Id[User]]): String =
    deaths.size match {
      case 0 => bold("No one has died.")
      case 1 => bold(s"${mapping.nameOf(deaths(0))} has died.")
      case _ => {
        val names = Grammar.conjunctionList(deaths.map { mapping.nameOf(_) }.sorted)
        bold(s"${names} have died.")
      }
    }

  private def winMessage(mapping: UserMapping, winnerIds: List[Id[User]]): String =
    winnerIds.size match {
      case 0 => bold("Everyone loses.")
      case 1 => bold(s"${mapping.nameOf(winnerIds(0))} wins.")
      case _ => {
        val names = Grammar.conjunctionList(winnerIds.map { mapping.nameOf(_) }.sorted)
        bold(s"${names} win.")
      }
    }

}
