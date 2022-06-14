
package com.mercerenies.werewolf
package game
package board

import org.javacord.api.entity.user.User

import id.Id
import role.Role

// Mixin helper trait
trait EndgameTestUtil {

  // Just to help with type inference since we only use Id[User] here.

  private def id(x: Long): Id[User] =
    Id.fromLong(x)

  private def id(x: Int): Id[User] =
    Id.fromLong(x)

  // Constructs an Endgame given a simple layout consisting of a left,
  // middle, and right card, as well as a list of player cards,
  // indexed starting at zero. Players are indexed starting from 0 as
  // well and sit in index order at the table.
  def createEndgame(
    left: Role,
    middle: Role,
    right: Role,
    playerCards: List[Role],
    deadPlayers: List[Long],
  ): Endgame = {
    val tablePositions = List(
      Position.left -> left,
      Position.middle -> middle,
      Position.right -> right,
    )
    val playerPositions = playerCards.zipWithIndex.map { (role, idx) => Position.Player(id(idx)) -> role }
    val board = Board.create(tablePositions ++ playerPositions)
    val playerIds = playerCards.indices.toList.map(id)
    val deadPlayerIds = deadPlayers.map(id)
    Endgame(
      board = board,
      playerList = playerIds,
      deaths = deadPlayerIds,
    )
  }

}

object EndgameTestUtil extends EndgameTestUtil
