
package com.mercerenies.werewolf
package game
package board
package snapshot

import id.Id
import role.{Role, RoleInstance}
import util.MultiSet

import org.javacord.api.entity.user.User

// An immutable snapshot of the board state at a given moment.
case class BoardSnapshot(
  val mapping: Map[Position, RoleSnapshot],
  val playerList: List[Id[User]],
) {

  export mapping.{get, apply}

  def toMap: Map[Position, RoleSnapshot] = mapping

}
