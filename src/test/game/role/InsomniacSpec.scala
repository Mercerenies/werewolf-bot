
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class InsomniacSpec extends GameplayUnitSpec {

  "The Insomniac role" should "not modify the board state in any way" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Tanner, Insomniac, Villager),
    )
    val (finalBoard, _, _, _) = playGame(board, List("right", "", ""))
    finalBoard should be (board)
  }

  it should "observe the final role card that the original Insomniac has now" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Insomniac),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)insomniac")

    val filtered = filterRecords(history)
    history.toList should have length (1)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)insomniac")

  }

  it should "provide no feedback to a drunk who becomes insomniac" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Insomniac,
      playerCards = List(Villager, Villager, Drunk),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "right"))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Drunk)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Insomniac)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should not include regex ("(?i)insomniac")

  }

  it should "inform an insomniac of their new card, even if it has been changed by a troublemaker" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Troublemaker, Insomniac),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", mockName(0) + " " + mockName(2), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Insomniac)
    finalBoard(id(1)).role should be (Troublemaker)
    finalBoard(id(2)).role should be (Villager)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)villager")

  }

  it should "inform an insomniac of their new card, even if it has been robbed" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Robber, Insomniac),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", mockName(2), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Insomniac)
    finalBoard(id(2)).role should be (Robber)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)robber")

  }

  it should "inform an insomniac of their new card, even if they were the target of a witch" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Witch, Insomniac),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "right " + mockName(2), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Insomniac)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Witch)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)tanner")

  }

}
