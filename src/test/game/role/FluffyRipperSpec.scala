
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class FluffyRipperSpec extends GameplayUnitSpec {

  import FluffyRipper.{Fluffy, Ripper}

  "The Fluffy and Ripper roles" should "be included in the global role list" in {
    Role.all should contain (Fluffy)
    Role.all should contain (Ripper)
  }

  they should "not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Fluffy, Villager, Ripper),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
    reveals shouldBe empty
  }

  they should "not change the state of the game when Fluffy is solo" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, Fluffy),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
  }

  they should "not change the state of the game when Ripper is solo" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, Ripper),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
  }

  they should "inform the werewolves of all of their teammates" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Werewolf, Fluffy, Ripper),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback.foreach { (_, x) =>
      x.mkString should include (mockName(0))
      x.mkString should include (mockName(1))
      x.mkString should include (mockName(2))
      x.mkString should include (mockName(3))
      x.mkString should include (mockName(4))
    }

  }

  it should "provide center-card feedback to a solo Fluffy" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Fluffy, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "right", ""))
    finalBoard should be (board)

    // The solo werewolf should see that the 'tanner' card is on the 'right'
    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex "(?i)tanner"
    feedback(id(1)).mkString should include regex "(?i)right"
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (2)
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)solo")
    filtered(0).displayText(SampleUserMapping(3)) should not include regex ("(?i)left|middle|right|tanner|villager")
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)solo")
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)right")
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)tanner")

  }

  it should "provide center-card feedback to a solo Ripper" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Ripper, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "right", ""))
    finalBoard should be (board)

    // The solo werewolf should see that the 'tanner' card is on the 'right'
    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex "(?i)tanner"
    feedback(id(1)).mkString should include regex "(?i)right"
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (2)
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)solo")
    filtered(0).displayText(SampleUserMapping(3)) should not include regex ("(?i)left|middle|right|tanner|villager")
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)solo")
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)right")
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)tanner")

  }

  it should "provide no center-card feedback to a solo Fluffy who fails to respond" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Fluffy, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include regex ("(?i)tanner|villager|left|middle|right")
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (2)
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)solo")
    filtered(1).displayText(SampleUserMapping(3)) should not include regex ("(?i)left|middle|right|tanner|villager")

  }

  it should "provide no center-card feedback to a solo Ripper who fails to respond" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Ripper, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include regex ("(?i)tanner|villager|left|middle|right")
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (2)
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)solo")
    filtered(1).displayText(SampleUserMapping(3)) should not include regex ("(?i)left|middle|right|tanner|villager")

  }

  it should "provide no center-card feedback to a solo Fluffy who explicitly responds 'none'" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Fluffy, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include regex ("(?i)tanner|villager|left|middle|right")
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "provide no center-card feedback to a solo Ripper who explicitly responds 'none'" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Ripper, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include regex ("(?i)tanner|villager|left|middle|right")
    feedback(id(2)) should be (FeedbackMessage.none)

  }

}
