
package com.mercerenies.werewolf
package game
package board

import role.{Role, RoleInstance}

import scala.collection.mutable.HashMap

// Invariant: Every RoleInstance contained on this board shall be
// coherent, where "coherent" is defined in RoleInstance.scala. This
// invariant is not enforced by the constructor, which is why the
// constructor is private. To make an instance of Board, use
// Board.create, which takes a Map[Position, Role] and constructs the
// RoleInstances correctly, ensuring that the invariant holds.
final class Board private(
  private val mapping: HashMap[Position, RoleInstance],
) {

}

object Board {

  def create(roles: Map[Position, Role]): Board =
    new Board(HashMap.from(roles.map { (pos, role) => (pos, role.createInstance()) }))

}
