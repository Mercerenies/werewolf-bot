
package com.mercerenies.werewolf
package command

import org.javacord.api.listener.interaction.SlashCommandCreateListener
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.DiscordApi

import scala.concurrent.{Future, ExecutionContext}
import scala.collection.JavaConverters.*
import scala.jdk.FutureConverters.*

final class CommandManager(
  private val commands: CommandList,
  private val api: DiscordApi,
)(
  using ExecutionContext,
) {

  private object Listener extends SlashCommandCreateListener {

    override def onSlashCommandCreate(event: SlashCommandCreateEvent): Unit = {
      val interaction = event.getSlashCommandInteraction
      val namedArgs: List[String] = interaction.getOptions.asScala.toList.map(_.getName)
      commands.find { _.name == interaction.getCommandName() }.foreach { matchingCommand =>
        matchingCommand.invoke(interaction, namedArgs)
      }
    }

  }

  api.bulkOverwriteGlobalApplicationCommands(commands.map(_.toBuilder).asJava).asScala.foreach { _ =>
    api.addSlashCommandCreateListener(Listener)
  }

}
