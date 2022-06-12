
package com.mercerenies.werewolf
package game
package board

import id.Id
import role.{Role, RoleInstance}

import org.javacord.api.entity.user.User

///// test me

// Whereas the Board class stores the actual state of the game,
// including player actions and stateful role specifics, an
// AssignmentBoard is a simple mapping from positions on the board
// (either table positions or players) to the proposed role that
// belongs to that player.
case class AssignmentBoard(
  val mapping: Map[Position, Role],
  val playerList: List[Id[User]],
  val unassignedRoles: List[Role],
) {

  export mapping.get

  def toMap: Map[Position, Role] = mapping

  // Precondition: this.get(position) == None
  private def assignRoleImpl(position: Position, role: Role): AssignmentBoard = {
    // If the role is in the unassigned roles list, then use that one.
    // Otherwise, take it from the mapping.
    if (unassignedRoles.contains(role)) {
      val newUnassignedRoles = util.deleteFirst(unassignedRoles, role)
      AssignmentBoard(
        mapping = mapping.updated(position, role),
        playerList = playerList,
        unassignedRoles = newUnassignedRoles,
      )
    } else {
      // Find it in the mapping
      mapping.find { (_, v) => v == role } match {
        case None => {
          // Role does not exist in this game, so ignore.
          this
        }
        case Some((k, v)) => {
          // Role was already assigned, so move it.
          AssignmentBoard(
            mapping = mapping - k + ((position, v)),
            playerList = playerList,
            unassignedRoles = unassignedRoles,
          )
        }
      }
    }
  }

  // Unassign the position if it's already in use, then assign the
  // role to it.
  def assignRole(position: Position, role: Role): AssignmentBoard =
    this.unassignRole(position).assignRoleImpl(position, role)

  def unassignRole(position: Position): AssignmentBoard = {
    val currentValue = get(position)
    val newUnassignedRoles = currentValue match {
      case None => unassignedRoles
      case Some(value) => value :: unassignedRoles
    }
    AssignmentBoard(
      mapping = mapping.removed(position),
      playerList = playerList,
      unassignedRoles = newUnassignedRoles,
    )
  }

}

object AssignmentBoard {

  // An initially empty assignment board based on a real game board.
  // No game information will be revealed in the resulting
  // AssignmentBoard object except that which is already public (like
  // the player list and the list of roles that are in play)
  def empty(board: Board, playerList: List[Id[User]]): AssignmentBoard =
    AssignmentBoard(
      mapping = Map(),
      playerList = playerList,
      unassignedRoles = board.roles,
    )

}
