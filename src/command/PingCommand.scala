
package com.mercerenies.werewolf

import org.javacord.api.interaction.{SlashCommand, SlashCommandBuilder}
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag

val PingCommand = Command(
  "ping",
  "A simple ping pong command!",
) { interaction =>
  interaction.createImmediateResponder()
    .setContent("Pong!")
    .setFlags(InteractionCallbackDataFlag.EPHEMERAL) // Only visible for the user which invoked the command
    .respond()
}
