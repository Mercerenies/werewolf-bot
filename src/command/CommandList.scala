
package com.mercerenies.werewolf

import org.javacord.api.DiscordApi
import org.javacord.api.interaction.{SlashCommand, SlashCommandBuilder}

final class CommandList(
  override val toList: List[Command],
) extends Seq[Command] {

  def register(api: DiscordApi): CommandManager =
    CommandManager(this, api)

  export toList.{apply, iterator, length}

}
