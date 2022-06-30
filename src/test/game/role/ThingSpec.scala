
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class ThingSpec extends GameplayUnitSpec {

  "The Thing role" should "be included in the global role list" in {
    Role.all should contain (Thing)
  }

  it should "receive no feedback and provide none if the Thing does not respond" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Thing, Villager),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should not include regex ("(?i)left|right|villager|werewolf")
    feedback(id(3)) should be (FeedbackMessage.none)

    reveals shouldBe empty

  }

  it should "receive no feedback and provide none if the Thing responds 'none'" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Thing, Villager),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should not include regex ("(?i)left|right|villager|werewolf")
    feedback(id(3)) should be (FeedbackMessage.none)

    reveals shouldBe empty

  }

  it should "bump the player to the right if requested" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Thing, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", "right", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)right")
    feedback(id(2)).mkString should not include regex ("(?i)villager|werewolf")
    feedback(id(3)).mkString should include regex ("(?i)thing")
    feedback(id(3)).mkString should include regex ("(?i)left")

    history.toVector should have length (1)
    history.toVector(0).displayText(SampleUserMapping(4)) should include (mockName(3))
    history.toVector(0).displayText(SampleUserMapping(4)) should include regex ("(?i)thing")
    history.toVector(0).displayText(SampleUserMapping(4)) should include regex ("(?i)right")

  }

  it should "bump the player to the left if requested" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Thing, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", "left", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex ("(?i)thing")
    feedback(id(1)).mkString should include regex ("(?i)right")
    feedback(id(2)).mkString should include regex ("(?i)left")
    feedback(id(2)).mkString should not include regex ("(?i)villager|werewolf")
    feedback(id(3)) should be (FeedbackMessage.none)

    history.toVector should have length (1)
    history.toVector(0).displayText(SampleUserMapping(4)) should include (mockName(1))
    history.toVector(0).displayText(SampleUserMapping(4)) should include regex ("(?i)thing")
    history.toVector(0).displayText(SampleUserMapping(4)) should include regex ("(?i)left")

  }

  it should "bump to the right, wrapping around" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Thing),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", "right"))
    finalBoard should be (board)

    feedback(id(0)).mkString should include regex ("(?i)thing")
    feedback(id(0)).mkString should include regex ("(?i)left")
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)right")
    feedback(id(2)).mkString should not include regex ("(?i)villager|werewolf")

  }

  it should "bump to the left, wrapping around" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Thing, Villager, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("left", "", ""))
    finalBoard should be (board)

    feedback(id(0)).mkString should include regex ("(?i)left")
    feedback(id(0)).mkString should not include regex ("(?i)villager|werewolf")
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)thing")
    feedback(id(2)).mkString should include regex ("(?i)right")

  }

}
