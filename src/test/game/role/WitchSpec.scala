
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class WitchSpec extends GameplayUnitSpec {

  "The Witch role" should "perform no action if a witch elects to do nothing" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Witch, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include (mockName(0))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(1)).mkString should not include regex ("(?i)villager|werewolf|tanner|left|middle|right")
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "perform no action if a witch fails to respond" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Witch, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include (mockName(0))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(1)).mkString should not include regex ("(?i)villager|werewolf|tanner|left|middle|right")
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "swap a player card with one in the center and be given information if a witch elects to act" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Witch, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "left " + mockName(0), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Witch)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should not include regex ("(?i)villager")
    feedback(id(1)).mkString should include regex ("(?i)werewolf")
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)werewolf")
    filtered(0).displayText(SampleUserMapping(3)) should not include regex ("(?i)tanner|villager")

  }

  it should "act before the troublemaker" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Witch, Tanner, Troublemaker),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "middle " + mockName(2), "", mockName(0) + " " + mockName(2)))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Tanner)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Witch)
    finalBoard(id(2)).role should be (Villager)
    finalBoard(id(3)).role should be (Troublemaker)

    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include regex ("(?i)werewolf")
    feedback(id(3)).mkString should include (mockName(0))
    feedback(id(3)).mkString should include (mockName(2))
    feedback(id(3)).mkString should not include regex ("(?i)werewolf|robber|witch|tanner|villager")

  }

  it should "act after the robber" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Witch, Tanner, Robber),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "middle " + mockName(2), "", mockName(2)))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Robber)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Witch)
    finalBoard(id(2)).role should be (Werewolf)
    finalBoard(id(3)).role should be (Tanner)

    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include regex ("(?i)werewolf")
    feedback(id(1)).mkString should not include regex ("(?i)robber|tanner|villager")
    feedback(id(3)).mkString should include (mockName(2))
    feedback(id(3)).mkString should include regex ("(?i)tanner")
    feedback(id(3)).mkString should not include regex ("(?i)werewolf|robber|witch|villager")

  }

}
