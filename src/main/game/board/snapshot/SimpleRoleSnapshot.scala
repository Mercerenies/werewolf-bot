
package com.mercerenies.werewolf
package game
package board
package snapshot

import id.Id
import role.Role
import util.MultiSet

import org.javacord.api.entity.user.User

case class SimpleRoleSnapshot(val role: Role) extends RoleSnapshot {

  export role.name

}
