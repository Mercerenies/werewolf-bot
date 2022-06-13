
package com.mercerenies.werewolf
package game
package parser
package assignment

import board.AssignmentBoard

case class Assignment(
  val position: NamedPosition,
  val role: MaybeRole,
) extends (AssignmentBoard => AssignmentBoard) {

  def apply(board: AssignmentBoard): AssignmentBoard =
    role.toOption match {
      case None => board.unassignRole(position.toPosition)
      case Some(role) => board.assignRole(position.toPosition, role)
    }

}
