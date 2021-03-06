
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import night.{ChoiceMessageHandler, NoInputNightMessageHandler}
import board.{Board, TablePosition, Position}

class MysticWolfSpec extends GameplayUnitSpec {

  "The Mystic Wolf role" should "be included in the global role list" in {
    Role.all should contain (MysticWolf)
  }

  it should "not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(MysticWolf, Villager, Werewolf),
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
      playerCards = List(MysticWolf, Tanner, Villager),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
  }

  it should "inform the werewolves of all of their teammates, including the Mystic Wolf" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Villager, MysticWolf),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(0)).mkString should include (mockName(0))
    feedback(id(0)).mkString should include (mockName(1))
    feedback(id(0)).mkString should not include (mockName(2))
    feedback(id(0)).mkString should include (mockName(3))
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(1)).mkString should include (mockName(3))
    feedback(id(3)).mkString should include (mockName(0))
    feedback(id(3)).mkString should include (mockName(1))
    feedback(id(3)).mkString should not include (mockName(2))
    feedback(id(3)).mkString should include (mockName(3))

  }

  it should "inform the werewolves of all of their teammates, including a copy-Mystic-Wolf" in {
    val board = createBoard(
      left = Werewolf,
      middle = MysticWolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Villager, Copycat),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", ("middle", "")))
    finalBoard should be (board)

    feedback(id(0)).mkString should include (mockName(0))
    feedback(id(0)).mkString should include (mockName(1))
    feedback(id(0)).mkString should not include (mockName(2))
    feedback(id(0)).mkString should include (mockName(3))
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(1)).mkString should include (mockName(3))
    feedback(id(3)).mkString should include (mockName(0))
    feedback(id(3)).mkString should include (mockName(1))
    feedback(id(3)).mkString should not include (mockName(2))
    feedback(id(3)).mkString should include (mockName(3))

  }

  it should "provide player card feedback to a Mystic Wolf" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Tanner, MysticWolf, Werewolf),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", mockName(0), ""))
    finalBoard should be (board)

    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex "(?i)tanner"

  }

  it should "provide player card feedback to a copy-Mystic-Wolf" in {
    val board = createBoard(
      left = MysticWolf,
      middle = Villager,
      right = Tanner,
      playerCards = List(Tanner, Copycat, Werewolf),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", ("left", mockName(0)), ""))
    finalBoard should be (board)

    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex "(?i)tanner"

  }

  it should "provide player card feedback to a solo Mystic Wolf" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Tanner, MysticWolf, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", mockName(0), ""))
    finalBoard should be (board)

    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex "(?i)tanner"

  }

  it should "provide player card feedback to a solo copy Mystic Wolf" in {
    val board = createBoard(
      left = MysticWolf,
      middle = Villager,
      right = Tanner,
      playerCards = List(Tanner, Copycat, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", ("left", mockName(0)), ""))
    finalBoard should be (board)

    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex "(?i)tanner"

  }

  it should "provide no card feedback to a Mystic Wolf who fails to respond" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(1)).mkString should not include regex ("(?i)tanner|villager")

  }

  it should "see the original cards correctly even if observing a drunk" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Drunk, MysticWolf, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("middle", mockName(0), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Drunk)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (MysticWolf)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex "(?i)drunk"

  }

  it should "see the original cards correctly even if a copy-mystic wolf is observing a drunk" in {
    val board = createBoard(
      left = Villager,
      middle = MysticWolf,
      right = Tanner,
      playerCards = List(Drunk, Copycat, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("middle", ("middle", mockName(0)), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Drunk)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (MysticWolf)
    finalBoard(id(1)).role should be (Copycat)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex "(?i)drunk"

  }

}
