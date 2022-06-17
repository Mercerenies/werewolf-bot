
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class TroublemakerSpec extends GameplayUnitSpec {

  "The Troublemaker role" should "perform no action if a troublemaker elects to do nothing" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Troublemaker, Tanner),
    )
    val (finalBoard, history, feedback) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include (mockName(0))
    feedback(id(1)).mkString should not include (mockName(1))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (1)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should not include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should not include (mockName(2))

  }

  it should "swap roles if a troublemaker elects to act" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Troublemaker, Tanner),
    )
    val (finalBoard, history, feedback) = playGame(board, List("", s"${mockName(0)} ${mockName(2)}", ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Tanner)
    finalBoard(id(1)).role should be (Troublemaker)
    finalBoard(id(2)).role should be (Villager)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should not include regex ("(?i)villager|tanner|werewolf")
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(2))
    filtered(0).displayText(SampleUserMapping(3)) should not include regex ("(?i)villager|tanner|werewolf")

  }

}
