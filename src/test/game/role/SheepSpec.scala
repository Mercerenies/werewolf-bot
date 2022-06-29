
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class SheepSpec extends GameplayUnitSpec {

  "The Sheep role" should "be included in the global role list" in {
    Role.all should contain (Sheep)
  }

  it should "not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Werewolf, Sheep, Werewolf, Minion),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)
    reveals shouldBe empty
  }

  it should "not change the state of the game, even if there are no werewolves" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Minion, Tanner, Sheep),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
  }

  it should "inform the sheep of an adjacent werewolf" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Werewolf, Sheep),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(3)).mkString should not include (mockName(0))
    feedback(id(3)).mkString should not include (mockName(2))
    feedback(id(3)).mkString should include regex ("(?i)at least one werewolf")

  }

  it should "inform the sheep of an adjacent werewolf wrapped around the player order" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Villager, Sheep),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(3)).mkString should not include (mockName(0))
    feedback(id(3)).mkString should not include (mockName(2))
    feedback(id(3)).mkString should include regex ("(?i)at least one werewolf")

  }

  it should "inform the sheep of an adjacent werewolf if there are werewolves on both sides" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Villager, Werewolf, Sheep),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(3)).mkString should not include (mockName(0))
    feedback(id(3)).mkString should not include (mockName(2))
    feedback(id(3)).mkString should include regex ("(?i)at least one werewolf")

  }

  it should "inform the sheep of an adjacent werewolf if the adjacent one is a werewolf variant" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, DreamWolf, Sheep),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(3)).mkString should not include (mockName(0))
    feedback(id(3)).mkString should not include (mockName(2))
    feedback(id(3)).mkString should include regex ("(?i)at least one werewolf")

  }

  it should "inform the sheep of no adjacent werewolves if there are no werewolves" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Sheep),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(3)).mkString should not include (mockName(0))
    feedback(id(3)).mkString should not include (mockName(2))
    feedback(id(3)).mkString should include regex ("(?i)no werewolves")

  }

  it should "inform the sheep of no adjacent werewolves if there are no werewolves adjacent" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Villager, Sheep),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(3)).mkString should not include (mockName(0))
    feedback(id(3)).mkString should not include (mockName(2))
    feedback(id(3)).mkString should include regex ("(?i)no werewolves")

  }

  it should "inform the sheep of the werewolves' absence before a troublemaker swaps" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Villager, Sheep, Troublemaker),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", "", mockName(1) + " " + mockName(2)))

    finalBoard(TablePosition.Left).role should be (Werewolf)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Werewolf)
    finalBoard(id(3)).role should be (Sheep)
    finalBoard(id(4)).role should be (Troublemaker)

    feedback(id(3)).mkString should include regex ("(?i)no werewolves")

  }

  it should "inform the sheep of the werewolves' presence before a troublemaker swaps" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Werewolf, Sheep, Troublemaker),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", "", mockName(1) + " " + mockName(2)))

    finalBoard(TablePosition.Left).role should be (Werewolf)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Werewolf)
    finalBoard(id(2)).role should be (Villager)
    finalBoard(id(3)).role should be (Sheep)
    finalBoard(id(4)).role should be (Troublemaker)

    feedback(id(3)).mkString should include regex ("(?i)at least one werewolf")

  }

  it should "inform the sheep of the werewolves' absence before a robber swaps" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Villager, Villager, Sheep, Robber),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", "", mockName(0)))

    finalBoard(TablePosition.Left).role should be (Werewolf)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Robber)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Villager)
    finalBoard(id(3)).role should be (Sheep)
    finalBoard(id(4)).role should be (Werewolf)

    feedback(id(3)).mkString should include regex ("(?i)no werewolves")

  }

  it should "inform the sheep of the werewolves' presence before a robber swaps" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Robber, Villager, Villager, Sheep, Werewolf),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List(mockName(4), "", "", "", ""))

    finalBoard(TablePosition.Left).role should be (Werewolf)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Villager)
    finalBoard(id(3)).role should be (Sheep)
    finalBoard(id(4)).role should be (Robber)

    feedback(id(3)).mkString should include regex ("(?i)at least one werewolf")

  }

}
