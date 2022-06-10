
package com.mercerenies.werewolf
package command

import org.javacord.api.DiscordApi
import org.javacord.api.interaction.{SlashCommand, SlashCommandBuilder}

import scala.concurrent.ExecutionContext

final class CommandList(
  override val toList: List[Command],
) extends Seq[Command] {

  def this(args: Command*) = this(args.toList)

  def register(api: DiscordApi)(using ExecutionContext): CommandManager =
    CommandManager(this, api)

  export toList.{apply, iterator, length}

}
