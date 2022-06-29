
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class ExposerSpec extends GameplayUnitSpec {

  "The Exposer role" should "be included in the global role list" in {
    Role.all should contain (Exposer)
  }

  it should "receive feedback and reveal a card publicly" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Exposer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", "right"))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)right")
    feedback(id(2)).mkString should include regex ("(?i)tanner")
    feedback(id(2)).mkString should not include regex ("(?i)villager|werewolf")

    reveals should be (Set(Position.right))

  }

  it should "receive no feedback and reveal nothing if they fail to answer" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Exposer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should not include regex ("(?i)left|middle|right|villager|werewolf|tanner")

    reveals shouldBe empty

  }

  it should "receive no feedback and reveal nothing if they explicitly answer 'none'" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Exposer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", "none"))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should not include regex ("(?i)left|middle|right|villager|werewolf|tanner")

    reveals shouldBe empty

  }

  it should "reveal the card that is present after a drunk acts" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Drunk, Exposer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "left", "left"))

    finalBoard(TablePosition.Left).role should be (Drunk)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Exposer)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)drunk")
    feedback(id(2)).mkString should not include regex ("(?i)villager|werewolf")

    reveals should be (Set(Position.left))

  }

  it should "reveal the card that is present after a witch acts" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Witch, Exposer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "middle " + mockName(2), "middle"))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Exposer)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Witch)
    finalBoard(id(2)).role should be (Werewolf)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)exposer")
    feedback(id(2)).mkString should not include regex ("(?i)werewolf|villager|tanner|witch")

    reveals should be (Set(Position.middle))

  }

  it should "reveal multiple cards if there are multiple exposers" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Exposer, Exposer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "middle", "left"))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex ("(?i)werewolf")
    feedback(id(2)).mkString should include regex ("(?i)villager")

    reveals should be (Set(Position.middle, Position.left))

  }

  it should "reveal a card only once if multiple exposers target the same card" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Exposer, Exposer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "right", "right"))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex ("(?i)tanner")
    feedback(id(2)).mkString should include regex ("(?i)tanner")

    reveals should be (Set(Position.right))

  }

}
