
package com.mercerenies.werewolf
package event

import id.Id
import command.Command
import state.{GameState, SignupState}

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.Nameable
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.listener.message.reaction.{ReactionAddListener, ReactionRemoveAllListener, ReactionRemoveListener}
import org.javacord.api.event.channel.TextChannelEvent
import org.javacord.api.event.message.reaction.{ReactionEvent, ReactionAddEvent, ReactionRemoveAllEvent, ReactionRemoveEvent}

import scala.collection.mutable.HashMap
import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.concurrent.{ExecutionContext, Future}

final class GamesReactionListener(
  private val games: GamesManager,
)(
  using ExecutionContext,
) extends ReactionAddListener with ReactionRemoveAllListener with ReactionRemoveListener {

  private def getGame(event: TextChannelEvent): Option[GameState] =
    GamesManager.toNamed(event.getChannel).flatMap { channel => games.getGame(Id(channel)) }

  private def delegateToGame(event: ReactionEvent): Unit =
    for {
      gameState <- getGame(event)
      message <- event.requestMessage.asScala
    } {
      gameState.onReactionsUpdated(message)
    }

  override def onReactionAdd(event: ReactionAddEvent): Unit =
    delegateToGame(event)

  override def onReactionRemove(event: ReactionRemoveEvent): Unit =
    delegateToGame(event)

  override def onReactionRemoveAll(event: ReactionRemoveAllEvent): Unit =
    delegateToGame(event)

}
