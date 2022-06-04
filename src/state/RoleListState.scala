
package com.mercerenies.werewolf
package state

import id.Id
import util.TextDecorator.*
import util.Emoji
import logging.Logging
import logging.LogEither.*
import name.{NameProvider, BaseNameProvider, DisplayNameProvider}
import command.CommandResponse

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.{TextChannel, Channel}
import org.javacord.api.entity.user.User
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.Nameable
import org.javacord.api.entity.Mentionable
import org.javacord.api.interaction.SlashCommandInteraction
import org.javacord.api.interaction.callback.InteractionImmediateResponseBuilder

import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.jdk.CollectionConverters.*
import scala.concurrent.{Future, ExecutionContext}

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

// A game in this state has closed signups and is now waiting for the
// host to choose a list of roles.
final class RoleListState(
  override val channelId: Id[TextChannel & Nameable],
  override val hostId: Id[User],
  private val players: List[User],
)(
  using ExecutionContext,
) extends GameState with Logging[RoleListState] {

  import scalaz.EitherT.eitherTHoist

  override def onReactionsUpdated(mgr: GamesManager, message: Message): Unit = {
    // No action
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    throw Exception("") /////

}
