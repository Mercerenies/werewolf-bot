
package com.mercerenies.werewolf

import command.{CommandList, Command}

import org.javacord.api.DiscordApi

import scala.concurrent.{Future, ExecutionContext}
import scala.jdk.FutureConverters.*

final class BotManager(
  private val config: BotConfig,
  private val api: DiscordApi,
)(
  using ExecutionContext
) {

  private val games = GamesManager()

  private val commands = CommandList(
    Command.Sub("wolfie", "Base for Werewolf-related commands")(games.commands: _*),
  )

  private val commandManager = commands.register(api)

}

object BotManager {

  def initialize(config: BotConfig)(using ExecutionContext): Future[BotManager] =
    config.produceApi().map { new BotManager(config, _) }

}
