
package com.mercerenies.werewolf
package state

import id.{Id, Ids, UserMapping}
import id.Ids.*
import util.TextDecorator.*
import util.Emoji
import logging.Logging
import logging.Logs.warningToLogger
import name.{NameProvider, BaseNameProvider, DisplayNameProvider}
import command.CommandResponse
import manager.GamesManager
import game.Rules
import game.board.{Board, PlayerOrder}
import game.role.Role
import game.role.instance.RoleInstance
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
  override val playerOrder: PlayerOrder,
)(
  using ExecutionContext,
) extends GameState(_gameProperties) with WithUserMapping {

  import RoleListState.logger
  import scalaz.EitherT.eitherTHoist

  // Don't need any DMs so don't bother.
  override val listeningPlayerList: List[Id[User]] =
    Nil

  private def isMessageRelevant(mgr: GamesManager, server: Server, message: Message): Boolean =
    // We only care about messages sent by the game host / server
    // admin and which ping the bot.
    (message.getAuthor.getId == hostId.toLong) &&
      (util.mentions(message, mgr.api.getYourself))

  private def requiredRoleCount = Rules.rolesNeeded(playerOrder.length)

  private def gameStartMessage(api: DiscordApi, server: Server, roleList: List[Role]): Future[String] =
    for {
      mapping <- getUserMapping(api)
      players <- playerOrder.toList.traverse { api.getUser(_) }
    } yield {
      val playerList = players.map(_.getDisplayName(server)).mkString(", ")
      val roleListSorted = RoleListState.sortRolesByBasePrecedence(mapping, roleList).map(_.name).mkString(", ")
      bold("Welcome to One Night Ultimate Werewolf") + "\n\n" +
        "The following players are participating (in order): " + playerList + "\n" +
        "The following roles are in play: " + roleListSorted + "\n" +
        bold("I am sending each player's role via DM now.")
    }

  // TODO Default message if you ping the bot in a channel that
  // doesn't have a game? (for all states, not just this one)
  override def onMessageCreate(mgr: GamesManager, message: Message): Unit = {
    val server = mgr.api.getServerFromMessage(message)
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

  private def setupGame(mgr: GamesManager, server: Server, roles: List[Role]): Future[Unit] = {
    val channel = mgr.api.getNamedTextChannel(channelId)
    for {
      mapping <- getUserMapping(mgr.api)
      board = Board.assignRoles(mapping, playerOrder.toList, roles)
      startMessage <- gameStartMessage(mgr.api, server, roles)
      _ <- channel.sendMessage(startMessage).asScala
      _ <- RoleListState.sendAllInitialDirectMessages(mgr.api, server, board)
    } yield {
      val newState = DuskPhaseState(gameProperties, playerOrder, board)
      mgr.updateGame(channelId, newState)
      ()
    }
  }

  override def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    Future.successful(CommandResponse.ephemeral("This game has already started and is waiting on a role list.").void)

  override def onStatusCommand(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]] =
    for {
      host <- mgr.api.getUser(hostId)
    } yield {
      CommandResponse.simple("The Werewolf game is currently " + bold("waiting on " + host.getMentionTag + " to submit a role list") + ".").void
    }

}

object RoleListState extends Logging[RoleListState] {

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
    user.sendMessage(roleInstance.fullIntroMessage(username)).asScala.void
  }

  private def sortRolesByBasePrecedence(mapping: UserMapping, roles: List[Role]): List[Role] =
    roles.map { role =>
      // Create temporary role instances (based on the real user
      // mapping) in order to get precedences.
      role.createInstance(mapping, None)
    }.sortBy { instance =>
      // Sort by night precedence first, then by voting precedence
      // (higher precedence goes first).
      (- instance.precedence, - instance.votesPrecedence)
    }.map { instance =>
      // We only want the roles, not the (mocked) instances.
      instance.role
    }

}
