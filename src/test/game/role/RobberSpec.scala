
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class RobberSpec extends GameplayUnitSpec {

  "The Robber role" should "perform no action if a robber elects to do nothing" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Robber, Tanner),
    )
    val (finalBoard, history, feedback) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include (mockName(0))
    feedback(id(1)).mkString should not include (mockName(1))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(1)).mkString should not include regex ("(?i)robber|villager|werewolf|tanner")
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "swap roles and be given information if a robber elects to act" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Robber, Tanner),
    )
    val (finalBoard, history, feedback) = playGame(board, List("", mockName(0), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Robber)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex ("(?i)villager")
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)villager")
    filtered(0).displayText(SampleUserMapping(3)) should not include regex ("(?i)tanner|werewolf")

  }

  it should "act before the troublemaker" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Werewolf, Robber, Tanner, Troublemaker),
    )
    val (finalBoard, history, feedback) = playGame(board, List("", "", mockName(0), "", mockName(0) + " " + mockName(1)))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Robber)
    finalBoard(id(2)).role should be (Villager)
    finalBoard(id(3)).role should be (Tanner)
    finalBoard(id(4)).role should be (Troublemaker)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include (mockName(0))
    feedback(id(2)).mkString should include regex ("(?i)villager")
    feedback(id(3)) should be (FeedbackMessage.none)
    feedback(id(4)).mkString should include (mockName(0))
    feedback(id(4)).mkString should include (mockName(1))
    feedback(id(4)).mkString should not include regex ("(?i)werewolf|robber|tanner|villager")

  }

}
