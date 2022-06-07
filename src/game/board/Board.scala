
package com.mercerenies.werewolf
package game
package board

import id.Id
import role.{Role, RoleInstance}

import org.javacord.api.entity.user.User

import scala.collection.concurrent.TrieMap

// The (private) constructor takes a HashMap and uses it mutably. Use
// Board.create or Board.apply to construct a Board directly.
final class Board private(
  private val mapping: TrieMap[Position, RoleInstance],
) {

  export mapping.{get, apply, update}

  def toMap: Map[Position, RoleInstance] =
    // Returns a shallow copy of the actual map
    Map.from(mapping)

  def playerRoleInstances: List[(Id[User], RoleInstance)] =
    mapping.flatMap { (k, v) => k.getUserId.map { (_, v) } }.toList

  def playerRoleAssignments: List[(Id[User], Role)] =
    mapping.flatMap { (k, v) => k.getUserId.map { (_, v.role) } }.toList

}

object Board {

  def apply(mapping: Iterable[(Position, RoleInstance)]) =
    new Board(TrieMap.from(mapping))

  def create(roles: Iterable[(Position, Role)]): Board =
    Board(roles.map { (pos, role) => (pos, role.createInstance()) })

  // Precondition: players.length + Rules.centerCards == roles.length
  def assignRoles(players: List[Id[User]], roles: List[Role]): Board = {
    // Validate precondition
    if (players.length + Rules.centerCards != roles.length) {
      throw new RuntimeException(s"In a ${players.length}-player game, expected ${players.length + Rules.centerCards} roles, but got ${roles.length} instead.")
    }

    val positions = Position.forGame(players)
    val roleAssignments = util.randomlyAssign(positions, roles)
    Board.create(roleAssignments)

  }

}
