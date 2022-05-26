
package com.mercerenies.werewolf

import command.{CommandList, PingCommand}

import org.javacord.api.{DiscordApi, DiscordApiBuilder}
import org.javacord.api.interaction.SlashCommand
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.util.logging.FallbackLoggerConfiguration

import scala.io.Source
import scala.concurrent.ExecutionContext

@main def main() = {
  given ExecutionContext = ExecutionContext.global
  val config = BotConfig()

  config.produceApi().foreach { api =>
    commands.register(api)
  }
}

private def commands: CommandList =
  CommandList(
    PingCommand,
  )

/*
    api.addMessageCreateListener { event =>
      if (event.getMessageContent().equalsIgnoreCase("!ping")) {
        event.getChannel().sendMessage("Pong!")
      }
    }
 */
