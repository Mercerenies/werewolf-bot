
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.Board
import TestGameRunner.{playGame, mockName}
import board.BoardTestUtil.createBoard

import scala.util.matching.Regex

class NightGameEvaluatorSpec extends UnitSpec {

  // Just to help with type inference since we only use Id[User] here.

  private def id(x: Long): Id[User] =
    Id.fromLong(x)

  private def id(x: Int): Id[User] =
    Id.fromLong(x)

  "NightGameEvaluator" should "provide no feedback and change none of the roles in an all-villager game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager),
    )
    val (finalBoard, feedback) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)
  }

  it should "provide no feedback and change none of the roles in an villager-tanner game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, Tanner),
    )
    val (finalBoard, feedback) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)
  }

  it should "provide feedback to the werewolves in a villager-werewolf game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Werewolf, Werewolf),
    )
    val (finalBoard, feedback) = playGame(board, List("", "left", "none"))
    finalBoard should be (board)

    // The two werewolves should be made aware of each other
    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include (mockName(2))

  }

  it should "provide center-card feedback to a solo werewolf" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Villager),
    )
    val (finalBoard, feedback) = playGame(board, List("", "right", ""))
    finalBoard should be (board)

    // The solo werewolf should see that the 'tanner' card is on the 'right'
    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex "(?i)tanner"
    feedback(id(1)).mkString should include regex "(?i)right"
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "provide empty feedback to a solo werewolf who does not respond" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Villager),
    )
    val (finalBoard, feedback) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include ("solo")
    feedback(id(1)).mkString should not include regex ("(?i)villager|tanner")
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "provide empty feedback to a solo werewolf who responds 'none'" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Villager),
    )
    val (finalBoard, feedback) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include ("solo")
    feedback(id(1)).mkString should not include regex ("(?i)villager|tanner")
    feedback(id(2)) should be (FeedbackMessage.none)

  }

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

}
