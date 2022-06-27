
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class WerewolfSpec extends GameplayUnitSpec {

  "The Werewolf role" should "not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Werewolf, Villager, Werewolf),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
    reveals shouldBe empty
  }

  it should "not change the state of the game when solo" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Werewolf, Tanner, Villager),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
  }

  it should "inform the werewolves of all of their teammates" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Werewolf),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)).mkString should include (mockName(0))
    feedback(id(0)).mkString should include (mockName(1))
    feedback(id(0)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(2)).mkString should include (mockName(0))
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include (mockName(2))

    val filtered = filterRecords(history)
    filtered should have length (3)
    filtered.foreach { rec =>
      rec.displayText(SampleUserMapping(3)) should include (mockName(0))
      rec.displayText(SampleUserMapping(3)) should include (mockName(1))
      rec.displayText(SampleUserMapping(3)) should include (mockName(2))
    }

  }

  it should "inform only the werewolves of their teammates" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)).mkString should include (mockName(0))
    feedback(id(0)).mkString should include (mockName(1))
    feedback(id(0)).mkString should not include (mockName(2))
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(2)) should be (FeedbackMessage.none)
  }

  it should "provide center-card feedback to a solo werewolf" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "right", ""))
    finalBoard should be (board)

    // The solo werewolf should see that the 'tanner' card is on the 'right'
    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex "(?i)tanner"
    feedback(id(1)).mkString should include regex "(?i)right"
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)right")
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)tanner")

  }

  it should "provide no center-card feedback to a solo werewolf who fails to respond" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include regex ("(?i)tanner|villager|left|middle|right")
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should not include regex ("(?i)left|middle|right|tanner|villager")

  }

  it should "provide no center-card feedback to a solo werewolf who explicitly responds 'none'" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include regex ("(?i)tanner|villager|left|middle|right")
    feedback(id(2)) should be (FeedbackMessage.none)

  }

}
