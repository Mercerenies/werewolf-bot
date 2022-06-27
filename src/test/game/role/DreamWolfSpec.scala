
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class DreamWolfSpec extends GameplayUnitSpec {

  "The Dream Wolf role" should "not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Werewolf, Villager, DreamWolf),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
    reveals shouldBe empty
  }

  it should "count as a werewolf but not be informed of the other werewolves" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, DreamWolf),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)).mkString should include (mockName(0))
    feedback(id(0)).mkString should include (mockName(1))
    feedback(id(0)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (2) // Dream wolf doesn't get a message.

  }

  it should "provide no feedback to a solo dream wolf" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, DreamWolf, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (0)

  }

}
