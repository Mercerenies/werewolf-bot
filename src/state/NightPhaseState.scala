
package com.mercerenies.werewolf
package state

import id.Id
import id.Ids.*
import util.TextDecorator.*
import util.Emoji
import logging.Logging
import logging.LogEither.*
import name.{NameProvider, BaseNameProvider, DisplayNameProvider}
import command.CommandResponse
import manager.GamesManager
import game.Rules
import game.board.Board
import game.role.Role
import game.parser.ListParser

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

final class NightPhaseState(
  override val channelId: Id[TextChannel & Nameable],
  override val hostId: Id[User],
  private val players: List[User],
  private val board: Board,
)(
  using ExecutionContext,
) extends GameState with Logging[RoleListState] {

  import scalaz.EitherT.eitherTHoist

  private val playerIds: List[Id[User]] =
    players.map { Id(_) }

  override val listeningPlayerList: List[Id[User]] =
    playerIds

  override def onReactionsUpdated(mgr: GamesManager, message: Message): Unit = {
    // No action
  }

  override def onMessageCreate(mgr: GamesManager, message: Message): Unit = {
    message.reply("It is currently nighttime. " + bold("Please do not post in this channel until day."))
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("It is currently night in this game.").void)

}

object NightPhaseState {

}