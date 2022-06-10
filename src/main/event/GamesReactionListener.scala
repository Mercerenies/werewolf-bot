
package com.mercerenies.werewolf
package event

import id.Id
import command.Command
import state.{GameState, SignupState}
import manager.GamesManager
import logging.Logging
import logging.Logs.*

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.Nameable
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.listener.message.reaction.{ReactionAddListener, ReactionRemoveAllListener, ReactionRemoveListener}
import org.javacord.api.event.channel.TextChannelEvent
import org.javacord.api.event.message.reaction.{ReactionEvent, ReactionAddEvent, ReactionRemoveAllEvent, ReactionRemoveEvent}

import scala.util.Try
import scala.collection.mutable.HashMap
import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.concurrent.{ExecutionContext, Future}

class GamesReactionListener(
  private val games: GamesManager,
)(
  using ExecutionContext,
) extends ReactionAddListener with ReactionRemoveAllListener with ReactionRemoveListener {

  import GamesReactionListener.logger

  private def getGame(event: TextChannelEvent): Option[GameState] =
    GamesManager.toNamed(event.getChannel).flatMap { channel => games.getGame(Id(channel)) }

  private def delegateToGame(event: ReactionEvent): Unit =
    for {
      gameState <- getGame(event)
      message <- event.requestMessage.asScala
    } {
      Try {
        gameState.onReactionsUpdated(games, message)
      }.logErrors(logger)
    }

  override def onReactionAdd(event: ReactionAddEvent): Unit =
    delegateToGame(event)

  override def onReactionRemove(event: ReactionRemoveEvent): Unit =
    delegateToGame(event)

  override def onReactionRemoveAll(event: ReactionRemoveAllEvent): Unit =
    delegateToGame(event)

}

object GamesReactionListener extends Logging[GamesReactionListener]
