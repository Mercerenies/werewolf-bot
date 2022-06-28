
package com.mercerenies.werewolf
package game
package board
package assignment

import IterableNormalizations.unordered
import id.Id
import role.*
import util.MultiSet

import org.javacord.api.entity.user.User

class AssignmentBoardSpec extends UnitSpec {

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
      playerOrder = PlayerOrder(List(id(0), id(1), id(2))),
      unassignedRoles = MultiSet(Villager),
    )
  }

  "An assignment board" should "convert back to the map from which it was constructed" in {
    val mapping: Map[Position, Role] = Map(
      Position.Table(TablePosition.Left) -> Villager,
      Position.Table(TablePosition.Middle) -> Werewolf,
    )
    val board = AssignmentBoard(mapping, PlayerOrder(List(id(0))), MultiSet.empty)
    board.toMap should be (mapping)
  }

  it should "allow querying for roles based on positions" in {
    val board = sampleBoard
    board.get(Position.Table(TablePosition.Left)) should be (Some(Villager))
    board.get(Position.Table(TablePosition.Middle)) should be (Some(Villager))
    board.get(Position.Table(TablePosition.Right)) should be (None)
    board.get(Position.Player(id(0))) should be (Some(Werewolf))
    board.get(Position.Player(id(1))) should be (Some(Villager))
    board.get(Position.Player(id(2))) should be (None)
  }

  it should "keep track of the player list in the correct order" in {
    sampleBoard.playerOrder should be (PlayerOrder(List(id(0), id(1), id(2))))
  }

  it should "allow unassigned roles to be assigned to empty slots" in {
    val board = sampleBoard.assignRole(Position.Player(id(2)), Villager)
    board.get(Position.Player(id(2))) should be (Some(Villager))
    board.get(Position.Player(id(1))) should be (Some(Villager))
    board.unassignedRoles should be (MultiSet())
  }

  it should "allow unassigned roles to be assigned to nonempty slots" in {
    val board = sampleBoard.assignRole(Position.Player(id(0)), Villager)
    board.get(Position.Player(id(0))) should be (Some(Villager))
    board.get(Position.Player(id(1))) should be (Some(Villager))
    board.unassignedRoles should be (MultiSet(Werewolf))
  }

  it should "allow assigned roles to be assigned to empty slots" in {
    val board = sampleBoard.assignRole(Position.Table(TablePosition.Right), Werewolf)
    board.get(Position.Player(id(0))) should be (None)
    board.get(Position.Player(id(1))) should be (Some(Villager))
    board.get(Position.Table(TablePosition.Right)) should be (Some(Werewolf))
    board.unassignedRoles should be (MultiSet(Villager))
  }

  it should "allow assigned roles to be assigned to nonempty slots" in {
    val board = sampleBoard.assignRole(Position.Player(id(1)), Werewolf)
    board.get(Position.Player(id(0))) should be (None)
    board.get(Position.Player(id(1))) should be (Some(Werewolf))
    board.get(Position.Table(TablePosition.Right)) should be (None)
    board.unassignedRoles should be (MultiSet(Villager, Villager))
  }

}
