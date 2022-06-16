
package com.mercerenies.werewolf
package game

import board.BoardTestUtil
import id.Id

import org.javacord.api.entity.user.User

// Helpers for unit testing classes which simulate runs of the night
// phase.
abstract class GameplayUnitSpec extends UnitSpec {

  def id(x: Long): Id[User] =
    Id.fromLong(x)

  def id(x: Int): Id[User] =
    Id.fromLong(x)

  export BoardTestUtil.createBoard
  export TestGameRunner.{playGame, mockName}

}
