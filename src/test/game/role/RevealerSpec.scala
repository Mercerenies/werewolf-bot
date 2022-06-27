
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import wincon.*
import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class RevealerSpec extends GameplayUnitSpec {

  "The Revealer role" should "receive feedback and reveal a card publicly" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Revealer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", mockName(1)))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include regex ("(?i)villager")

    reveals should be (Set(Position.Player(id(1))))

  }

  it should "refuse to reveal the card if they see a werewolf" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Revealer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", mockName(1)))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include regex ("(?i)werewolf")

    reveals shouldBe empty

  }

  it should "refuse to reveal the card if they see a tanner" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Tanner, Revealer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", mockName(1)))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include regex ("(?i)tanner")

    reveals shouldBe empty

  }

  it should "receive no feedback and reveal nothing if they fail to answer" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Revealer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should not include regex ("(?i)villager|werewolf|tanner")

    reveals shouldBe empty

  }

  it should "receive no feedback and reveal nothing if they explicitly answer 'none'" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Revealer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", "none"))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should not include regex ("(?i)villager|werewolf|tanner")

    reveals shouldBe empty

  }

  it should "reveal the card that is present after a drunk acts" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Drunk, Revealer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "left", mockName(1)))

    finalBoard(TablePosition.Left).role should be (Drunk)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Revealer)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include regex ("(?i)villager")
    feedback(id(2)).mkString should not include regex ("(?i)drunk|werewolf|tanner")

    reveals should be (Set(Position.Player(id(1))))

  }

  it should "reveal multiple cards if there are multiple revealers" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Revealer, Revealer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", mockName(0), mockName(1)))
    finalBoard should be (board)

    feedback(id(2)).mkString should include regex ("(?i)villager")
    feedback(id(3)).mkString should include regex ("(?i)seer")

    reveals should be (Set(Position.Player(id(0)), Position.Player(id(1))))

  }

  it should "reveal a card only once if multiple revealers target the same card" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Revealer, Revealer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", mockName(0), mockName(0)))
    finalBoard should be (board)

    feedback(id(2)).mkString should include regex ("(?i)villager")
    feedback(id(3)).mkString should include regex ("(?i)villager")

    reveals should be (Set(Position.Player(id(0))))

  }

  it should "reveal a Paranormal Investigator card, even if it happens to be werewolf-aligned" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Werewolf, ParanormalInvestigator, Revealer),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", mockName(1), mockName(2)))
    finalBoard should be (board)

    feedback(id(2)).mkString should include regex ("(?i)werewolf")
    feedback(id(3)).mkString should include regex ("(?i)paranormal investigator")

    finalBoard(id(2)).winCondition should be (WerewolfWinCondition)

    reveals should be (Set(Position.Player(id(2))))

  }

}
