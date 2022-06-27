
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class TannerSpec extends GameplayUnitSpec {

  "The Tanner role" should "provide no feedback and not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Tanner, Seer, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", ""))

    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)

  }

  it should "not record any game events in the records" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Tanner, Tanner, Tanner),
    )
    val (finalBoard, history, _, _) = playGame(board, List("", "", ""))

    finalBoard should be (board)

    filterRecords(history) shouldBe empty

  }

}
