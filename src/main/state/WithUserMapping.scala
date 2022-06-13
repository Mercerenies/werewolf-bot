
package com.mercerenies.werewolf
package state

import util.Cell
import id.{Id, UserMapping}
import id.Ids.*
import command.CommandResponse
import manager.GamesManager
import properties.GameProperties
import timer.Cancellable

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
// UserMapping.
transparent trait WithUserMapping extends GameState {

  val playerIds: List[Id[User]]

  private val _userMapping: Cell[Option[UserMapping]] = Cell(None)

  // Get the mapping if it's already been computed, or None if not.
  def userMappingOption: Option[UserMapping] =
    _userMapping.value

  // Force the mapping.
  def getUserMapping(api: DiscordApi)(using ExecutionContext): Future[UserMapping] =
    userMappingOption match {
      case Some(x) => Future.successful(x)
      case None => {
        val channel = api.getServerTextChannel(channelId)
        for {
          userMapping <- UserMapping.fromServer(api, channel.getServer, playerIds)
        } yield {
          _userMapping.value = Some(userMapping)
          userMapping
        }
      }
    }

}
