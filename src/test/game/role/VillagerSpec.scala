
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class VillagerSpec extends GameplayUnitSpec {

  "The Villager role" should "provide no feedback and not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", ""))

    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "record no events in the records" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))

    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    filterRecords(history) shouldBe empty

  }

}
