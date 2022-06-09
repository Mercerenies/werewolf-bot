
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

}
