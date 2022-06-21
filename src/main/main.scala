
package com.mercerenies.werewolf

import command.{CommandList, PingCommand}
import manager.BotManager
import util.Pandoc

import org.apache.logging.log4j.{Logger, LogManager}

import org.javacord.api.{DiscordApi, DiscordApiBuilder}
import org.javacord.api.interaction.SlashCommand
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.util.logging.FallbackLoggerConfiguration

import scala.concurrent.ExecutionContext

@main def main() = {
  given ExecutionContext = ExecutionContext.global

  // Check for Pandoc capabilities.
  if (!Pandoc.exists) {
    logger.info("Note: Pandoc is not on the command line; embedded TXT log uploads will not be available.")
  }

  // Initialize bot.
  val config = BotConfig()
  BotManager.initialize(config)
  logger.info("Bot initialization complete.")

}

private val logger: Logger = LogManager.getRootLogger()
