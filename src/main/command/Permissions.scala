
package com.mercerenies.werewolf
package command

import id.Id
import command.CommandResponse

import org.javacord.api.entity.user.User
import org.javacord.api.entity.server.Server

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

// Helpers for monadically dealing with permissions.
object Permissions {

  private val notAdminMessage: String =
    "You must be a server admin to do that."

  private val notHostMessage: String =
    "You must be the game host to do that."

  private val notAdminOrHostMessage: String =
    "You must be a server admin or the game host to do that."

  def mustBeAdmin(server: Server, user: User)(okay: => CommandResponse[Unit]): CommandResponse[Unit] =
    if (server.isAdmin(user)) {
      okay
    } else {
      CommandResponse.ephemeral(notAdminMessage).void
    }

  // Note to users: Are you sure you don't want mustBeAdminOrHost
  // instead? It would be very odd to allow host but not admin
  // privileges for a particular command.
  def mustBeHost(hostId: Id[User], user: User)(okay: => CommandResponse[Unit]): CommandResponse[Unit] =
    if (user.getId == hostId.toLong) {
      okay
    } else {
      CommandResponse.ephemeral(notHostMessage).void
    }

  def mustBeAdminOrHost(server: Server, hostId: Id[User], user: User)(okay: => CommandResponse[Unit]): CommandResponse[Unit] =
    if ((server.isAdmin(user)) || (user.getId == hostId.toLong)) {
      okay
    } else {
      CommandResponse.ephemeral(notAdminOrHostMessage).void
    }

}
