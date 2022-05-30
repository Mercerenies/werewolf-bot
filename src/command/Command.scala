
package com.mercerenies.werewolf
package command

import org.javacord.api.interaction.{SlashCommand, SlashCommandBuilder, SlashCommandInteraction, SlashCommandOption, SlashCommandOptionType}
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag

import scala.collection.JavaConverters.*

sealed trait Command {
  def name: String
  def description: String
  def toBuilder: SlashCommandBuilder
  def toCommandOption: SlashCommandOption

  def invoke(interaction: SlashCommandInteraction, namedArgs: List[String]): Unit

}

object Command {

  case class Term(
    override val name: String,
    override val description: String,
  )(
    val perform: (SlashCommandInteraction) => Unit,
  ) extends Command {

    override def toBuilder: SlashCommandBuilder =
      SlashCommand.`with`(name, description)

    override def toCommandOption: SlashCommandOption =
      SlashCommandOption.create(SlashCommandOptionType.SUB_COMMAND, name, description)

    override def invoke(interaction: SlashCommandInteraction, namedArgs: List[String]) =
      perform(interaction)

  }

  case class Sub(
    override val name: String,
    override val description: String,
  )(
    val subcommands: Command*
  ) extends Command {

    override def toBuilder: SlashCommandBuilder =
      val subcommandOpts = subcommands.map { _.toCommandOption }
      SlashCommand.`with`(name, description, subcommandOpts.asJava)

    override def toCommandOption: SlashCommandOption =
      val subcommandOpts = subcommands.map { _.toCommandOption }
      SlashCommandOption.createWithOptions(SlashCommandOptionType.SUB_COMMAND_GROUP, name, description, subcommandOpts.asJava)

    override def invoke(interaction: SlashCommandInteraction, namedArgs: List[String]) =
      namedArgs match {
        case Nil => {
          respondWithError(interaction, "Not enough arguments.")
        }
        case head :: namedArgs => {
          subcommands.find { _.name == head }.foreach { _.invoke(interaction, namedArgs) }
        }
      }

  }

  private def respondWithError(interaction: SlashCommandInteraction, message: String) =
    interaction.createImmediateResponder()
      .setContent(message)
      .setFlags(InteractionCallbackDataFlag.EPHEMERAL) // Only visible for the user which invoked the command
      .respond()


}
