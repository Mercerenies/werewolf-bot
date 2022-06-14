
package com.mercerenies.werewolf
package game
package board

import org.javacord.api.entity.user.User

import id.Id
import role.Role

// Mixin helper trait
trait BoardTestUtil {

  // Just to help with type inference since we only use Id[User] here.

  private def id(x: Long): Id[User] =
    Id.fromLong(x)

  private def id(x: Int): Id[User] =
    Id.fromLong(x)

  // Creates a board with the given center cards and the players with
  // IDs 0 up to (and excluding) playerCards.length having the
  // indicated roles.
  def createBoard(
    left: Role,
    middle: Role,
    right: Role,
    playerCards: List[Role],
  ): Board = {
    val tablePositions = List(
      Position.left -> left,
      Position.middle -> middle,
      Position.right -> right,
    )
    val playerPositions = playerCards.zipWithIndex.map { (role, idx) => Position.Player(id(idx)) -> role }
    Board.create(tablePositions ++ playerPositions)
  }

}

object BoardTestUtil extends BoardTestUtil
