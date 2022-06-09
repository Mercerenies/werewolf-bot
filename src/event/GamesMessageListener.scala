
package com.mercerenies.werewolf
package event

import id.{Id, Messages}
import command.Command
import state.{GameState, SignupState}
import manager.GamesManager

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

class GamesMessageListener(
  private val games: GamesManager,
)(
  using ExecutionContext,
) extends MessageCreateListener {

  private def getGame(event: TextChannelEvent): Option[GameState] =
    GamesManager.toNamed(event.getChannel).flatMap { channel => games.getGame(Id(channel)) }

  private def delegateToGame(event: MessageCreateEvent): Unit =
    getGame(event).foreach { gameState =>
      val message = event.getMessage
      gameState.onMessageCreate(games, message)
    }

  override def onMessageCreate(event: MessageCreateEvent): Unit = {
    if (!Messages.wasSentByThisBot(event)) {
      delegateToGame(event)
    }
  }

}
