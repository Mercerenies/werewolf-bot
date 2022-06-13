
package com.mercerenies.werewolf
package state

import util.Cell
import id.{Id, UserMapping}
import id.Ids.*
import command.CommandResponse
import manager.GamesManager
import properties.GameProperties
import timer.Cancellable
import game.parser.assignment.NamedUser
import game.board.Board

import org.javacord.api.DiscordApi
import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.interaction.SlashCommandInteraction

import java.util.concurrent.LinkedBlockingQueue

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import scala.concurrent.{Future, ExecutionContext}
import scala.concurrent.duration.Duration
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*

// Mixin for game states which have access to a (lazily-initialized)
// list of NamedUser.
transparent trait WithNamedUserList extends GameState {

  val playerIds: List[Id[User]]

  private val _userList: LazyValue[DiscordApi, List[NamedUser]] =
    LazyValue { api =>
      val channel = api.getServerTextChannel(channelId)
      val server = channel.getServer
      for {
        players <- playerIds.traverse { api.getUser(_) }
      } yield {
        players.map { NamedUser.fromUser(_, server) }
      }
    }

  export _userList.{getValue => getUserList}

}