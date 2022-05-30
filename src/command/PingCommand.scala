
package com.mercerenies.werewolf
package command

import org.javacord.api.interaction.{SlashCommand, SlashCommandBuilder}
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag

val PingCommand = Command.Sub("ping", "A simple ping pong command!")(
  Command.Term(
    "a",
    "A simple ping pong command!",
  ) { interaction =>
    interaction.createImmediateResponder()
      .setContent("Pong A!")
      .setFlags(InteractionCallbackDataFlag.EPHEMERAL) // Only visible for the user which invoked the command
      .respond()
  },
  Command.Term(
    "b",
    "Another one",
  ) { interaction =>
    interaction.createImmediateResponder()
      .setContent("Pong B!")
      .setFlags(InteractionCallbackDataFlag.EPHEMERAL) // Only visible for the user which invoked the command
      .respond()
  }
)
