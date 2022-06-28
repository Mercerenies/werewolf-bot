
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
import game.board.{Board, PlayerOrder}

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

  val playerOrder: PlayerOrder

  val board: Board

  private val _assignmentParser: LazyValue[DiscordApi, AssignmentParser] =
    LazyValue { api =>
      val channel = api.getServerTextChannel(channelId)
      AssignmentParser.fromBoard(api, channel.getServer, board, playerOrder)
    }

  export _assignmentParser.{getValue => getAssignmentParser}

}
