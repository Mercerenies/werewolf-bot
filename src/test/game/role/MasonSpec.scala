
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class MasonSpec extends GameplayUnitSpec {

  "The Mason role" should "perform no action" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Mason, Mason),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)

  }

  it should "see who the other masons are" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Mason, Mason),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include (mockName(2))

    val filtered = filterRecords(history)
    filtered should have length (2)
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(2))
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(2))

  }

  it should "see who the other masons are before a troublemaker swaps them" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Mason, Mason, Troublemaker),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", "", mockName(0) + " " + mockName(1)))
    finalBoard(id(0)).role should be (Mason)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Mason)
    finalBoard(id(3)).role should be (Troublemaker)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include (mockName(2))

  }

  it should "only indicate mason cards belonging to players, not those in the center" in {
    val board = createBoard(
      left = Mason,
      middle = Werewolf,
      right = Mason,
      playerCards = List(Villager, Werewolf, Mason),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should not include regex ("(?i)left|middle|right")
    feedback(id(2)).mkString should not include regex ("(?i)solo")

  }

}
