
package com.mercerenies.werewolf
package game
package board

import IterableNormalizations.unordered
import id.{Id, UserMapping}
import role.*
import util.MultiSet

import org.javacord.api.entity.user.User

class StandardAssignmentBoardFormatterSpec extends UnitSpec {

  private def id(x: Long): Id[User] =
    Id.fromLong(x)

  private def sampleBoard: AssignmentBoard = {
    // Note: Table position Right and player ID 2 are unassigned.
    val mapping = Map(
      Position.Table(TablePosition.Left) -> Villager,
      Position.Table(TablePosition.Middle) -> Villager,
      Position.Player(id(0)) -> Werewolf,
      Position.Player(id(1)) -> Villager,
    )
    AssignmentBoard(
      mapping = mapping,
      playerList = List(id(0), id(2), id(1)),
      unassignedRoles = MultiSet(Villager),
    )
  }

  private object MockUserMapping extends UserMapping {

    override def keys: Iterable[Id[User]] =
      throw new AssertionError("MockUserMapping.keys not implemented")

    override def get(id: Id[User]): Option[User] =
      throw new AssertionError("MockUserMapping.get not implemented")

    override def getName(id: Id[User]): Option[String] =
      Some(s"User ${id.toLong}")

  }

  "StandardAssignmentBoardFormatter" should "format an empty AssignmentBoard correctly" in {
    val formatter = StandardAssignmentBoardFormatter(MockUserMapping)
    formatter.format(AssignmentBoard(Map(), List(id(0), id(1), id(2)), MultiSet.empty)) should be (
      "**Role Board**\nUser 0 (-)\nUser 1 (-)\nUser 2 (-)\nCenter (-; -; -)\nUnplaced ()"
    )
  }

  it should "format a sample AssignmentBoard correctly" in {
    val formatter = StandardAssignmentBoardFormatter(MockUserMapping)
    formatter.format(sampleBoard) should be (
      "**Role Board**\nUser 0 (Werewolf)\nUser 2 (-)\nUser 1 (Villager)\nCenter (Villager; Villager; -)\nUnplaced (Villager)"
    )
  }

}
