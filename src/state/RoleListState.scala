
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
import game.role.{Role, RoleInstance}
import game.parser.ListParser
import properties.GameProperties

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
// host to choose a list of roles. When the host does so (by pinging
// the bot with an appropriately-formatted message), the game moves to
// NightPhaseState.
final class RoleListState(
  _gameProperties: GameProperties,
  private val players: List[User],
)(
  using ExecutionContext,
) extends GameState(_gameProperties) with Logging[RoleListState] {

  import scalaz.EitherT.eitherTHoist

  private val playerIds: List[Id[User]] =
    players.map { Id(_) }

  // Don't need any DMs so don't bother.
  override val listeningPlayerList: List[Id[User]] = Nil

  override def onReactionsUpdated(mgr: GamesManager, message: Message): Unit = {
    // No action
  }

  private def isMessageRelevant(mgr: GamesManager, server: Server, message: Message): Boolean =
    // We only care about messages sent by the game host / server
    // admin and which ping the bot.
    (message.getAuthor.getId == host.getId) &&
      (util.mentions(message, Id(mgr.api.getYourself)))

  private def requiredRoleCount = Rules.rolesNeeded(players.length)

  private def gameStartMessage(server: Server, roleList: List[Role]): String =
    bold("Welcome to One Night Ultimate Werewolf") + "\n\n" +
      "The following players are participating: " + players.map(_.getDisplayName(server)).mkString(", ") + "\n" +
      "The following roles are in play: " + roleList.map(_.name).mkString(", ") + "\n" +
      bold("I am sending each player's role via DM now.") + "\n" +
      bold("It is nighttime. Day will begin in 48 hours.") // TODO Configurable day start

  // TODO Default message if you ping the bot in a channel that
  // doesn't have a game? (for all states, not just this one)
  override def onMessageCreate(mgr: GamesManager, message: Message): Unit = {
    mgr.api.getServerFromMessage[[A] =>> Scalaz.Id[A]](message).run match {
      case -\/(err) => {
        logger.warn(err)
      }
      case \/-(server) => {
        if (isMessageRelevant(mgr, server, message)) {
          val parser = RoleListState.listParser
          parser.parse(message.getContent) match {
            case -\/(err) => {
              message.reply(err)
            }
            case \/-(roles) if roles.length != requiredRoleCount => {
              message.reply(s"Sorry, but I'm expecting ${requiredRoleCount} role(s). You gave me a list of ${roles.length} role(s).")
            }
            case \/-(roles) => {
              message.reply("Role list accepted.").asScala.flatMap { _ =>
                setupGame(mgr, server, roles)
              }
            }
          }
        }
      }
    }
  }

  override def onDirectMessageCreate(mgr: GamesManager, user: User, message: Message): Unit = {
    // No action
  }

  private def setupGame(mgr: GamesManager, server: Server, roles: List[Role]): Future[Unit] = {
    val board = Board.assignRoles(playerIds, roles)
    for {
      _ <- channel.sendMessage(gameStartMessage(server, roles)).asScala
      _ <- RoleListState.sendAllInitialDirectMessages(mgr.api, server, board)
    } yield {
      val newState = NightPhaseState(gameProperties, players, board)
      mgr.updateGame(Id(channel), newState)
      ()
    }
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("This game has already started and is waiting on a role list.").void)

}

object RoleListState {

  val validRoles: List[Role] = Role.all

  private val listParser: ListParser[Role] =
    ListParser(validRoles, "role")

  private def sendAllInitialDirectMessages(api: DiscordApi, server: Server, board: Board)(using ExecutionContext): Future[Unit] =
    board.playerRoleInstances.toList.traverse { (userId, roleInstance) =>
      for {
        user <- api.getUser(userId)
        _ <- sendInitialDirectMessage(server, user, roleInstance)
      } yield {
        ()
      }
    }.void

  private def sendInitialDirectMessage(server: Server, user: User, roleInstance: RoleInstance)(using ExecutionContext): Future[Unit] = {
    val username = user.getDisplayName(server)
    for {
      _ <- user.sendMessage(roleInstance.role.fullIntroMessage(username)).asScala
      _ <- user.sendMessage(roleInstance.nightHandler.initialNightMessage).asScala
    } yield {
      ()
    }
  }

}
