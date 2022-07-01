
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
import wincon.*

class CursedSpec extends GameplayUnitSpec {

  "The Cursed role" should "be included in the global role list" in {
    Role.all should contain (Cursed)
  }

  it should "not be considered a werewolf by default" in {
    val inst = Cursed.createInstance(SampleUserMapping(4), None)
    inst.isTurned should be (false)
    inst.seenAs should be (Nil)
    inst.winCondition should be (TownWinCondition)
  }

  it should "be considered a werewolf after turning" in {
    val inst = Cursed.createInstance(SampleUserMapping(4), None)
    inst.turnToWerewolf()
    inst.isTurned should be (true)
    inst.seenAs should be (List(GroupedRoleIdentity.Werewolf))
    inst.winCondition should be (WerewolfWinCondition)
  }

  it should "provide no feedback and not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Cursed),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", ""))

    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    reveals shouldBe (empty)

    filterRecords(history) shouldBe empty

  }

  it should "leave a Cursed as a town player if no werewolves vote for them" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Werewolf, Cursed),
    )
    val votes = votals(0 -> 1, 1 -> 2, 2 -> 1, 3 -> 0)
    val (deaths, history) = runVotes(board, votes)

    board(id(3)).asInstanceOf[Cursed.Instance].isTurned should be (false)

  }

  it should "leave a Cursed as a town player if there are no werewolves" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Cursed),
    )
    val votes = votals(0 -> 1, 1 -> 2, 2 -> 1, 3 -> 0)
    val (deaths, history) = runVotes(board, votes)

    board(id(3)).asInstanceOf[Cursed.Instance].isTurned should be (false)

  }

  it should "turn a Cursed player to werewolf if all werewolves vote for them" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Werewolf, Cursed),
    )
    val votes = votals(0 -> 1, 1 -> 3, 2 -> 3, 3 -> 0)
    val (deaths, history) = runVotes(board, votes)

    board(id(3)).asInstanceOf[Cursed.Instance].isTurned should be (true)

  }

  it should "turn a Cursed player to werewolf if at least one werewolf votes for them" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Werewolf, Werewolf, Cursed),
    )
    val votes = votals(0 -> 1, 1 -> 3, 2 -> 0, 3 -> 0)
    val (deaths, history) = runVotes(board, votes)

    board(id(3)).asInstanceOf[Cursed.Instance].isTurned should be (true)

  }

}
