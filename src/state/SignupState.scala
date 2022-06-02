
package com.mercerenies.werewolf
package state

import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.user.User
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.Nameable
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

}

object SignupState {

  def createGame(channel: TextChannel & Nameable, host: User)(using ExecutionContext): Future[SignupState] =
    val text = s"${host.getMentionTag} has started a game of One Night Ultimate Werewolf in this channel."
    for (
      message <- channel.sendMessage(text).asScala
    ) yield {
      SignupState(Id(channel), Id(host), Id(message))
    }

  def createGameStartMessage(hostId: Id[User])(interaction: SlashCommandInteraction)(using ExecutionContext): Future[InteractionImmediateResponseBuilder] = {
    val api = interaction.getApi
    for (
      user <- api.getUserById(hostId.toLong).asScala.map(_.nn)
    ) yield {
      val response = s"${user.getMentionTag} has started a game of One Night Ultimate Werewolf in this channel."
      interaction.createImmediateResponder()
        .setContent(response)
    }
  }

}
