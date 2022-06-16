
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

  export mapping.get

  def toMap: Map[Position, RoleSnapshot] = mapping

  def apply(x: Position): RoleSnapshot =
    toMap.apply(x)

  def apply(x: TablePosition): RoleSnapshot =
    toMap.apply(Position.Table(x))

  def apply(x: Id[User]): RoleSnapshot =
    toMap.apply(Position.Player(x))

}
