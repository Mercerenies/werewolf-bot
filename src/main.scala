
package com.mercerenies.werewolf

import command.{CommandList, PingCommand}
import manager.BotManager

import org.javacord.api.{DiscordApi, DiscordApiBuilder}
import org.javacord.api.interaction.SlashCommand
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.util.logging.FallbackLoggerConfiguration

import scala.concurrent.ExecutionContext

@main def main() = {
  given ExecutionContext = ExecutionContext.global
  val config = BotConfig()
  BotManager.initialize(config)
/*
  config.produceApi().foreach { api =>
    commands.register(api)
  }
 */
}
