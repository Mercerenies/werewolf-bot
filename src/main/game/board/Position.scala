
package com.mercerenies.werewolf
package game
package board

import id.Id

import org.javacord.api.entity.user.User

import scala.math.Ordering

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

  def ordering(playerList: List[Id[User]]): Ordering[Position] =
    Ordering.by {
      case Table(tablePosition) => (0, tablePosition.ordinal)
      case Player(id) => (1, playerList.indexOf(id))
    }

  def ordering(playerOrder: PlayerOrder): Ordering[Position] =
    ordering(playerOrder.toList)

  val left: Position = Position.Table(TablePosition.Left)
  val middle: Position = Position.Table(TablePosition.Middle)
  val right: Position = Position.Table(TablePosition.Right)

}
