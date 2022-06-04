
package com.mercerenies.werewolf
package manager

import command.{CommandList, Command}
import event.GamesReactionListener

import org.javacord.api.DiscordApi

import scala.concurrent.{Future, ExecutionContext}
import scala.jdk.FutureConverters.*

final class BotManager(
  private val config: BotConfig,
  private val api: DiscordApi,
)(
  using ExecutionContext
) {

  private val games = GamesManager(api)

  private val commands = CommandList(
    Command.Sub("wolfie", "Base for Werewolf-related commands")(games.commands: _*),
  )

  private val commandManager = commands.register(api)

  private val reactionListener = GamesReactionListener(games)

  api.addReactionAddListener(reactionListener)
  api.addReactionRemoveListener(reactionListener)
  api.addReactionRemoveAllListener(reactionListener)

}

object BotManager {

  def initialize(config: BotConfig)(using ExecutionContext): Future[BotManager] =
    config.produceApi().map { new BotManager(config, _) }

}
