
package com.mercerenies.werewolf
package game
package role

import command.{Command, ArgumentType}
import util.TextDecorator.*

import org.javacord.api.interaction.{SlashCommand, SlashCommandBuilder, SlashCommandInteraction, SlashCommandOption, SlashCommandOptionType}
import org.javacord.api.interaction.callback.InteractionCallbackDataFlag

class RoleCommands(_roleList: List[Role]) {

  val roleList = _roleList.sortBy { _.name }

  // TODO Consider pagination
  val ListCommand = Command.Term(
    "list",
    "List all roles available in the game",
  ) { interaction =>
    interaction.createImmediateResponder()
      .setContent("These are all of the roles I support:\n" + formattedRoleList)
      .respond()
  }

  val DescribeCommand = Command.Term(
    "describe",
    "Describe a role",
    List(
      SlashCommandOption.create(SlashCommandOptionType.STRING, "role", "The name of the One Night role to describe", true),
    ),
  ) { interaction =>
    val roleName = ArgumentType.StringArg.getArgument(interaction.getArguments().get(0))
    val response = roleList.find { _.name.toLowerCase == roleName.toLowerCase } match {
      case None => s"I don't know about any roles called '${roleName}'"
      case Some(role) => roleDescription(role)
    }
    interaction.createImmediateResponder()
      .setContent(response)
      .respond()
  }

  private def formattedRoleList: String =
    bulletedList(roleList.map { _.name })

  private def roleDescription(role: Role) =
    longCode(
      "\n" + role.name + "\n\n" +
        role.inspiration.description + "\n\n" +
        role.introBlurb + "\n\n" +
        role.baseWinCondition.blurb + "\n\n"
    )

}

object RoleCommands extends RoleCommands(Role.all)
