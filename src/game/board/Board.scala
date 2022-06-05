
package com.mercerenies.werewolf
package game
package board

import role.{Role, RoleInstance}

import scala.collection.mutable.HashMap

// The (private) constructor takes a HashMap and uses it mutably. Use
// Board.create or Board.apply to construct a Board directly.
final class Board private(
  private val mapping: HashMap[Position, RoleInstance],
) {

}

object Board {

  def apply(mapping: Map[Position, RoleInstance]) =
    new Board(HashMap.from(mapping))

  def create(roles: Map[Position, Role]): Board =
    Board(roles.map { (pos, role) => (pos, role.createInstance()) })

}
