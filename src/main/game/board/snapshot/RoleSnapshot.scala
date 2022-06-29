
package com.mercerenies.werewolf
package game
package board
package snapshot

import id.Id
import role.Role
import util.MultiSet

import org.javacord.api.entity.user.User

// A snapshot of a role at a given moment. For most roles, this is
// simply the name of the role (SimpleRoleSnapshot), but some roles
// like Doppelganger will keep track of state here.
trait RoleSnapshot {

  def name: String

}
