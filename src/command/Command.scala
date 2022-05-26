
package com.mercerenies.werewolf
package command

import org.javacord.api.interaction.{SlashCommand, SlashCommandBuilder, SlashCommandInteraction}

case class Command(
  val name: String,
  val description: String,
)(
  val perform: (SlashCommandInteraction) => Unit
) {

  def toBuilder: SlashCommandBuilder =
    SlashCommand.`with`(name, description)

}
