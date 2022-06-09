
package com.mercerenies.werewolf
package event

import id.{Id, Messages}
import command.Command
import state.{GameState, SignupState}
import manager.GamesManager
import logging.Logging

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.Nameable
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.listener.message.MessageCreateListener
import org.javacord.api.event.channel.TextChannelEvent
import org.javacord.api.event.message.MessageCreateEvent

import scala.collection.mutable.HashMap
import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.concurrent.{ExecutionContext, Future}

class GamesDirectMessageListener(
  private val games: GamesManager,
)(
  using ExecutionContext,
) extends MessageCreateListener with Logging[GamesDirectMessageListener] {

  // TODO We break ties arbitrarily here. Do it smarter if the player
  // is in multiple games (look for context clues / the name of the
  // game channel / something)
  private def getRelevantGame(event: TextChannelEvent): Option[(GameState, User)] =
    event.getPrivateChannel.toScala match {
      case None => None // This message is not a DM
      case Some(channel) => {
        channel.getRecipient.toScala match {
          case None => {
            // Wtf, Discord?
            logger.warn(s"Got DM ${event} but could not identify recipient of message.")
            None
          }
          case Some(user) => {
            games.getGamesForUser(Id(user)).headOption.map((_, user))
          }
        }
      }
    }

  private def delegateToGame(event: MessageCreateEvent): Unit =
    getRelevantGame(event).foreach { (gameState, user) =>
      val message = event.getMessage
      gameState.onDirectMessageCreate(games, user, message)
    }

  override def onMessageCreate(event: MessageCreateEvent): Unit = {
    if (!Messages.wasSentByThisBot(event)) {
      delegateToGame(event)
    }
  }

}
