
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class MinionSpec extends GameplayUnitSpec {

  import FluffyRipper.{Fluffy, Ripper}

  "The Minion role" should "be included in the global role list" in {
    Role.all should contain (Minion)
  }

  it should "not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Werewolf, Villager, Werewolf, Minion),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)
    reveals shouldBe empty
  }

  it should "not change the state of the game, even if there are no werewolves" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Minion, Tanner, Villager),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
  }

  it should "inform the minion of the werewolf team" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Werewolf, Minion),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(0)).mkString should include (mockName(0))
    feedback(id(0)).mkString should include (mockName(1))
    feedback(id(0)).mkString should include (mockName(2))
    feedback(id(0)).mkString should not include (mockName(3))
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should not include (mockName(3))
    feedback(id(2)).mkString should include (mockName(0))
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include (mockName(2))
    feedback(id(2)).mkString should not include (mockName(3))
    feedback(id(3)).mkString should include (mockName(0))
    feedback(id(3)).mkString should include (mockName(1))
    feedback(id(3)).mkString should include (mockName(2))

    val filtered = filterRecords(history)
    history.toVector should have length (4)
    filtered should have length (4)
    filtered.foreach { rec =>
      rec.displayText(SampleUserMapping(3)) should include (mockName(0))
      rec.displayText(SampleUserMapping(3)) should include (mockName(1))
      rec.displayText(SampleUserMapping(3)) should include (mockName(2))
    }
    // Third message should be about the minion
    filtered(3).displayText(SampleUserMapping(3)) should include regex ("(?i)minion")

  }

  it should "inform the minion of Fluffy and Ripper" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Ripper, Werewolf, Minion, Villager, Fluffy),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(3)).mkString should include (mockName(0))
    feedback(id(3)).mkString should include (mockName(1))
    feedback(id(3)).mkString should include (mockName(2))
    feedback(id(3)).mkString should not include (mockName(3))
    feedback(id(3)).mkString should not include (mockName(4))
    feedback(id(3)).mkString should include (mockName(5))

  }

  it should "inform the minion of a Dream Wolf" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Werewolf, Minion, Villager, DreamWolf),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(3)).mkString should include (mockName(0))
    feedback(id(3)).mkString should include (mockName(1))
    feedback(id(3)).mkString should include (mockName(2))
    feedback(id(3)).mkString should not include (mockName(3))
    feedback(id(3)).mkString should not include (mockName(4))
    feedback(id(3)).mkString should include (mockName(5))

  }

  it should "inform the minion of a Mystic Wolf" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Werewolf, Minion, Villager, MysticWolf),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", "", ""))
    finalBoard should be (board)

    feedback(id(3)).mkString should include (mockName(0))
    feedback(id(3)).mkString should include (mockName(1))
    feedback(id(3)).mkString should include (mockName(2))
    feedback(id(3)).mkString should not include (mockName(3))
    feedback(id(3)).mkString should not include (mockName(4))
    feedback(id(3)).mkString should include (mockName(5))

  }

  it should "inform a copy-minion of the werewolf team" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Minion,
      playerCards = List(Werewolf, Werewolf, Werewolf, Copycat),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", ("right", "")))
    finalBoard should be (board)

    feedback(id(0)).mkString should include (mockName(0))
    feedback(id(0)).mkString should include (mockName(1))
    feedback(id(0)).mkString should include (mockName(2))
    feedback(id(0)).mkString should not include (mockName(3))
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should not include (mockName(3))
    feedback(id(2)).mkString should include (mockName(0))
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include (mockName(2))
    feedback(id(2)).mkString should not include (mockName(3))
    feedback(id(3)).mkString should include (mockName(0))
    feedback(id(3)).mkString should include (mockName(1))
    feedback(id(3)).mkString should include (mockName(2))

  }

}
