
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class NightGameEvaluatorSpec extends GameplayUnitSpec {

  it should "provide feedback to a seer who looks in the middle" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Villager),
    )
    val (finalBoard, feedback) = playGame(board, List("", "right middle", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex "(?i)middle.*werewolf"
    feedback(id(1)).mkString should include regex "(?i)right.*tanner"
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "provide empty feedback to a seer who does not look" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Seer),
    )
    val (finalBoard, feedback) = playGame(board, List("", "", "none"))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include regex ("(?i)werewolf|villager|tanner")
    feedback(id(2)).mkString should not include regex ("(?i)werewolf|villager|tanner")

  }

  it should "provide feedback to a seer who does looks at another player's card" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Tanner),
    )
    val (finalBoard, feedback) = playGame(board, List("", mockName(2), ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include regex "(?i)tanner"
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "perform no action if a troublemaker elects to do nothing" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Troublemaker, Tanner),
    )
    val (finalBoard, feedback) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include (mockName(0))
    feedback(id(1)).mkString should not include (mockName(1))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "swap roles if a troublemaker elects to act" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Troublemaker, Tanner),
    )
    val (finalBoard, feedback) = playGame(board, List("", s"${mockName(0)} ${mockName(2)}", ""))

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

  }

}
