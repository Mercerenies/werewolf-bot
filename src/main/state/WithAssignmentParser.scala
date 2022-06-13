
package com.mercerenies.werewolf
package state

import util.Cell
import id.{Id, UserMapping}
import id.Ids.*
import command.CommandResponse
import manager.GamesManager
import properties.GameProperties
import timer.Cancellable
import game.parser.assignment.AssignmentParser
import game.board.Board

import org.javacord.api.DiscordApi
import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.interaction.SlashCommandInteraction

import java.util.concurrent.LinkedBlockingQueue

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*

// Mixin for game states which have access to a (lazily-initialized)
// AssignmentParser.
transparent trait WithAssignmentParser extends GameState {

  val playerIds: List[Id[User]]

  val board: Board

  private val _assignmentParser: Cell[Option[AssignmentParser]] = Cell(None)

  // Get the parser if it's already been computed, or None if not.
  def assignmentParserOption: Option[AssignmentParser] =
    _assignmentParser.value

  // Force the parser.
  def getAssignmentParser(api: DiscordApi)(using ExecutionContext): Future[AssignmentParser] =
    assignmentParserOption match {
      case Some(x) => Future.successful(x)
      case None => {
        val channel = api.getServerTextChannel(channelId)
        for {
          assignmentParser <- AssignmentParser.fromBoard(api, channel.getServer, board, playerIds)
        } yield {
          _assignmentParser.value = Some(assignmentParser)
          assignmentParser
        }
      }
    }

}
