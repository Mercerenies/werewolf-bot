
package com.mercerenies.werewolf
package game
package board

import id.Id
import role.{Role, RoleInstance}

import org.javacord.api.entity.user.User

final class Board(
  private val toMap: Map[Position, RoleInstance],
) {

  export toMap.get

  // Returns the roles in alphabetical order by name.
  def roles: List[Role] =
    toMap.values.map { _.role }.toList.sortBy { _.name }

  def playerRoleInstances: List[(Id[User], RoleInstance)] =
    toMap.flatMap { (k, v) => k.getUserId.map { (_, v) } }.toList

  def playerRoleAssignments: List[(Id[User], Role)] =
    toMap.flatMap { (k, v) => k.getUserId.map { (_, v.role) } }.toList

  def apply(x: Position): RoleInstance =
    toMap.apply(x)

  def apply(x: TablePosition): RoleInstance =
    toMap.apply(Position.Table(x))

  def apply(x: Id[User]): RoleInstance =
    toMap.apply(Position.Player(x))

  override def equals(that: Any): Boolean =
    that match {
      case that: Board => this.toMap == that.toMap
      case _ => false
    }

  override def hashCode: Int =
    ("Board", toMap).hashCode

}

object Board {

  def apply(mapping: Iterable[(Position, RoleInstance)]) =
    new Board(Map.from(mapping))

  def apply(mapping: Map[Position, RoleInstance]) =
    new Board(mapping)

  def create(roles: Iterable[(Position, Role)]): Board =
    Board(roles.map { (pos, role) => (pos, role.createInstance()) })

  // Precondition: Rules.rolesNeeded(players.length) == roles.length
  def assignRoles(players: List[Id[User]], roles: List[Role]): Board = {
    // Validate precondition
    if (Rules.rolesNeeded(players.length) != roles.length) {
      throw new RuntimeException(s"In a ${players.length}-player game, expected ${Rules.rolesNeeded(players.length)} roles, but got ${roles.length} instead.")
    }

    val positions = Position.forGame(players)
    val roleAssignments = util.randomlyAssign(positions, roles)
    Board.create(roleAssignments)

  }

}
