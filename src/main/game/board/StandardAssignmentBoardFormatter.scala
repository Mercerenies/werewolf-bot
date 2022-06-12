
package com.mercerenies.werewolf
package game
package board

import id.UserMapping
import util.TextDecorator.*

///// test me

class StandardAssignmentBoardFormatter(
  private val userMapping: UserMapping,
) extends AssignmentBoardFormatter {

  override def format(board: AssignmentBoard): String =
    bold("Role Board") + "\n" +
      playerLines(board) + "\n" +
      centerLines(board) + "\n" +
      unplacedLines(board)

  private def playerLines(board: AssignmentBoard): String =
    board.playerList.map { playerId =>
      val playerName = userMapping.nameOf(playerId)
      val playerRole = board.getRoleName(Position.Player(playerId))
      s"${userMapping.nameOf(playerId)} (${playerRole})"
    }.mkString("\n")

  private def centerLines(board: AssignmentBoard): String = {
    val centerPositions = TablePosition.all map { Position.Table(_) }
    val centerRoles = centerPositions.map(board.getRoleName).mkString("; ")
    s"Center (${centerRoles})"
  }

  private def unplacedLines(board: AssignmentBoard): String = {
    val roleList = board.unassignedRoles.toList.map { _.name }.sorted.mkString(", ")
    s"Unplaced (${roleList})"
  }

}
