
package com.mercerenies.werewolf
package game
package board
package snapshot

import id.Id
import role.{Role, RoleInstance}
import util.MultiSet

import org.javacord.api.entity.user.User

case class CopiedRoleSnapshot(
  val originalRole: Role,
  val copiedRole: RoleSnapshot,
) extends RoleSnapshot {

  override def name: String =
    s"${originalRole.name} [${copiedRole.name}]"

}
