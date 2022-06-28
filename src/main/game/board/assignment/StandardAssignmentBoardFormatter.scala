
package com.mercerenies.werewolf
package game
package board
package assignment

import id.UserMapping
import util.TextDecorator.*

class StandardAssignmentBoardFormatter(
  private val userMapping: UserMapping,
) extends AssignmentBoardFormatter {

  override def format(board: AssignmentBoard): String =
    bold("Role Board") + "\n" +
      playerLines(board) + "\n" +
      centerLines(board) + "\n" +
      unplacedLines(board)

  private def playerLines(board: AssignmentBoard): String =
    board.playerOrder.map { playerId =>
      val playerName = userMapping.nameOf(playerId)
      val playerRole = board.getRoleName(Position.Player(playerId))
      s"${userMapping.nameOf(playerId)} (${playerRole})"
    }.mkString("\n")

  // TODO Alpha wolf table position
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
