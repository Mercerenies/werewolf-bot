
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}
import IterableNormalizations.*

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

  it should "follow the usual voting rules for majority" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager),
    )
    val votes = votals(0 -> 1, 1 -> 2, 2 -> 2)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(2)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toList shouldBe empty

  }

  it should "follow the usual voting rules for majority on a split vote" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager),
    )
    val votes = votals(0 -> 1, 1 -> 2, 2 -> 2, 3 -> 1)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(1, 2)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toList shouldBe empty

  }

  it should "follow the usual voting rules for majority on a null vote" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager),
    )
    val votes = votals(0 -> 1, 1 -> 2, 2 -> 3, 3 -> 0)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List()) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toList shouldBe empty

  }

}
