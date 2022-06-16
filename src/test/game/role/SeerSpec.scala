
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class SeerSpec extends GameplayUnitSpec {

  "The Seer role" should "not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Werewolf, Seer, Werewolf),
    )
    val (finalBoard, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)
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
