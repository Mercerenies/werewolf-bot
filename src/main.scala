
package com.mercerenies.werewolf

import org.javacord.api.{DiscordApi, DiscordApiBuilder}
import org.javacord.api.interaction.SlashCommand
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag
import org.javacord.api.util.logging.FallbackLoggerConfiguration

import scala.io.Source

private def getToken() =
  Source.fromFile("token.txt").mkString.stripLineEnd

@main def main() = {
  val token = getToken()
  val api = DiscordApiBuilder().setToken(token).login().join()
  // slash command
  //SlashCommand.`with`("ping", "A simple ping pong command!").createGlobal(api).join()
  // Add a listener which answers with "Pong!" if someone writes "!ping"
  api.addMessageCreateListener { event =>
    if (event.getMessageContent().equalsIgnoreCase("!ping")) {
      event.getChannel().sendMessage("Pong!")
    }
  }

  api.addSlashCommandCreateListener { event =>
    val slashCommandInteraction = event.getSlashCommandInteraction()
    if (slashCommandInteraction.getCommandName().equals("ping")) {
      slashCommandInteraction.createImmediateResponder()
        .setContent("Pong!")
        .setFlags(InteractionCallbackDataFlag.EPHEMERAL) // Only visible for the user which invoked the command
        .respond();
    }
  }
}
