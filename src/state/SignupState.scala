
package com.mercerenies.werewolf
package state

import TextDecorator.*

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.Nameable
import org.javacord.api.entity.Mentionable
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder

import scala.jdk.FutureConverters.*
import scala.concurrent.{Future, ExecutionContext}

// Parent trait for the states a game can be in.
final class SignupState(
  override val channelId: Id[TextChannel & Nameable],
  override val hostId: Id[User],
  private val gameStartMessage: Id[Message],
) extends GameState {

  override def onReactionsUpdated(message: Message): Unit = {}

}

object SignupState {

  private def gameStartMessage(host: Mentionable): String =
    s"${host.getMentionTag} has started a game of One Night Ultimate Werewolf in this channel. " +
    bold("Signups are open.") + " React to this post to join the game."

  def createGame(channel: TextChannel & Nameable, host: User)(using ExecutionContext): Future[SignupState] =
    val text = gameStartMessage(host)
    for (
      message <- channel.sendMessage(text).asScala
    ) yield {
      SignupState(Id(channel), Id(host), Id(message))
    }

}
