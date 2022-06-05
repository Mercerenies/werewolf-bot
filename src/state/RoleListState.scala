
package com.mercerenies.werewolf
package state

import id.Id
import util.TextDecorator.*
import util.Emoji
import logging.Logging
import logging.LogEither.*
import name.{NameProvider, BaseNameProvider, DisplayNameProvider}
import command.CommandResponse
import manager.GamesManager
import game.Rules
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

  private def isMessageRelevant(mgr: GamesManager, message: Message): Boolean =
    // We only care about messages sent by the game host and which
    // ping the bot.
    (message.getAuthor.getId == hostId.toLong) &&
      (util.mentions(message, Id(mgr.api.getYourself)))

  private def requiredRoleCount = Rules.rolesNeeded(players.length)

  // TODO Default message if you ping the bot in a channel that
  // doesn't have a game?
  override def onMessageCreate(mgr: GamesManager, message: Message): Unit = {
    if (isMessageRelevant(mgr, message)) {
      val parser = RoleListState.listParser
      parser.parse(message.getContent) match {
        case -\/(err) => {
          message.reply(err)
        }
        case \/-(roles) if roles.length != requiredRoleCount => {
          message.reply(s"Sorry, but I'm expecting ${requiredRoleCount} role(s). You gave me a list of ${roles.length} role(s).")
        }
        case \/-(roles) => {
          /////
          println(s"I got ${roles}!")
          message.reply("Thank you!")
        }
      }
    }
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("This game has already started and is waiting on a role list.").void)

}

object RoleListState {

  val validRoles: List[Role] = Role.all

  private val listParser: ListParser[Role] =
    ListParser(validRoles, "role")

}
