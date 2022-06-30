
package com.mercerenies.werewolf
package game
package role

import command.Command
import util.TextDecorator.*

import org.javacord.api.interaction.{SlashCommand, SlashCommandBuilder}
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

  private def formattedRoleList: String =
    bulletedList(roleList.map { _.name })

}

object RoleCommands extends RoleCommands(Role.all)
