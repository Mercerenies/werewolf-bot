
package com.mercerenies.werewolf
package game
package board

import IterableNormalizations.unordered
import id.Id

import org.javacord.api.entity.user.User

class PositionSpec extends UnitSpec {

  private def id(x: Long): Id[User] =
    Id.fromLong(x)

  "The Position enum" should "allow querying of the user ID" in {
    Position.Table(TablePosition.Left).getUserId should be (None)
    Position.Player(id(100)).getUserId should be (Some(id(100)))
  }

  "The Position singleton" should "construct an initial list of game positions from a list of users" in {
    val users = List(id(1), id(2), id(3))
    val positions = Position.forGame(users)

    positions should equal (List(
      Position.Table(TablePosition.Left),
      Position.Table(TablePosition.Middle),
      Position.Table(TablePosition.Right),
      Position.Player(id(1)),
      Position.Player(id(2)),
      Position.Player(id(3)),
    )) (after being unordered)
  }

}
