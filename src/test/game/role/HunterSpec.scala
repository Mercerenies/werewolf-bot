
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

class HunterSpec extends GameplayUnitSpec {

  "The Hunter role" should "be included in the global role list" in {
    Role.all should contain (Hunter)
  }

  it should "provide no feedback and not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Hunter),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", ""))

    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    reveals shouldBe (empty)

    filterRecords(history) shouldBe empty

  }

  it should "not influence voting if the hunter is not killed" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Hunter),
    )
    val votes = votals(0 -> 1, 1 -> 2, 2 -> 1, 3 -> 0)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(1)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toList shouldBe empty

  }

  it should "not influence voting if nobody is killed" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Hunter),
    )
    val votes = votals(0 -> 3, 1 -> 2, 2 -> 1, 3 -> 0)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List()) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toList shouldBe empty

  }

  it should "kill the target if the hunter is killed" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Hunter),
    )
    val votes = votals(0 -> 3, 1 -> 3, 2 -> 3, 3 -> 0)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(0, 3)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toVector should have length (1)
    history.toVector(0).displayText(SampleUserMapping(4)) should include (mockName(0))

  }

  it should "kill the target if a paranormal investigator who copied a hunter is killed" in {
    // PIs don't copy hunters in actual gameplay, since hunters are
    // town-aligned. But the interaction is, according to the code,
    // well-defined, so we're going to test it.
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, ParanormalInvestigator, Hunter),
    )
    board(id(3)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Hunter.createInstance(SampleUserMapping(5), Some(id(3))))

    val votes = votals(0 -> 3, 1 -> 3, 2 -> 3, 3 -> 0, 4 -> 2)
    val (deaths, _) = runVotes(board, votes)

    deaths.dead should equal (List(0, 3)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

  }

  it should "kill the target if the hunter is killed in a split vote" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Hunter),
    )
    val votes = votals(0 -> 4, 1 -> 2, 2 -> 4, 3 -> 2, 4 -> 1)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(1, 2, 4)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toVector should have length (1)
    history.toVector(0).displayText(SampleUserMapping(5)) should include (mockName(1))

  }

  it should "kill multiple targets if multiple hunters are killed" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Hunter, Hunter),
    )
    val votes = votals(0 -> 4, 1 -> 4, 2 -> 5, 3 -> 5, 4 -> 0, 5 -> 1)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(0, 1, 4, 5)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toVector should have length (2)
    history.toVector(0).displayText(SampleUserMapping(6)) should include (mockName(0))
    history.toVector(1).displayText(SampleUserMapping(6)) should include (mockName(1))

  }

  it should "kill one target if one of multiple hunters is killed" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Hunter, Hunter),
    )
    val votes = votals(0 -> 5, 1 -> 5, 2 -> 5, 3 -> 5, 4 -> 0, 5 -> 1)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(1, 5)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toVector should have length (1)
    history.toVector(0).displayText(SampleUserMapping(6)) should include (mockName(1))

  }

  it should "kill a chain of targets if hunters point at each other" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Hunter, Hunter, Hunter),
    )
    val votes = votals(0 -> 4, 1 -> 4, 2 -> 4, 3 -> 5, 4 -> 3, 5 -> 0)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(0, 3, 4, 5)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toVector should have length (3)
    history.toVector(0).displayText(SampleUserMapping(6)) should include (mockName(3))
    history.toVector(1).displayText(SampleUserMapping(6)) should include (mockName(5))
    history.toVector(2).displayText(SampleUserMapping(6)) should include (mockName(0))

  }

  it should "kill a chain of targets if hunters point at each other in a cycle" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Hunter, Hunter, Hunter),
    )
    val votes = votals(0 -> 4, 1 -> 4, 2 -> 4, 3 -> 5, 4 -> 3, 5 -> 4)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(3, 4, 5)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toVector should have length (2)
    history.toVector(0).displayText(SampleUserMapping(6)) should include (mockName(3))
    history.toVector(1).displayText(SampleUserMapping(6)) should include (mockName(5))

  }

  it should "kill a chain of targets if hunters and copy-hunters point at each other in a cycle" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Hunter, Copycat, Hunter),
    )

    board(id(4)).asInstanceOf[Copycat.Instance].copiedRole =
      Some(Hunter.createInstance(SampleUserMapping(6), Some(id(4))))

    val votes = votals(0 -> 4, 1 -> 4, 2 -> 4, 3 -> 5, 4 -> 3, 5 -> 4)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(3, 4, 5)) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toVector should have length (2)
    history.toVector(0).displayText(SampleUserMapping(6)) should include (mockName(3))
    history.toVector(1).displayText(SampleUserMapping(6)) should include (mockName(5))

  }

}
