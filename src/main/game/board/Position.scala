
package com.mercerenies.werewolf
package game
package board

import id.Id

import org.javacord.api.entity.user.User

// A position on the board where a role card shall sit.
enum Position {
  // A card in the middle of the table.
  case Table(val side: TablePosition)
  // A card in front of a player.
  case Player(val playerId: Id[User])

  def getUserId: Option[Id[User]] =
    this match {
      case Table(_) => None
      case Player(playerId) => Some(playerId)
    }

}

object Position {

  def forGame(playerList: List[Id[User]]): List[Position] = {
    val tablePositions = board.TablePosition.all.map { Position.Table(_) }
    val userPositions = playerList.map { Position.Player(_) }
    tablePositions ++ userPositions
  }

  // Sort a list of positions: Table positions go first in their
  // natural ordering, then players in the order given in the player
  // list.
  def sortPositions(playerList: List[Id[User]], positions: List[Position]): List[Position] = {
    def sortIndex(pos: Position): (Int, Long) =
      pos match {
        case Table(tablePosition) => (0, tablePosition.ordinal)
        case Player(id) => (1, playerList.indexOf(id))
      }
    positions.sortBy(sortIndex)
  }

  val left: Position = Position.Table(TablePosition.Left)
  val middle: Position = Position.Table(TablePosition.Middle)
  val right: Position = Position.Table(TablePosition.Right)

}
