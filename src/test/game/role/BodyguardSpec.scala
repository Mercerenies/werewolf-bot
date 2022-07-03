
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

class BodyguardSpec extends GameplayUnitSpec {

  "The Bodyguard role" should "be included in the global role list" in {
    Role.all should contain (Bodyguard)
  }

  it should "provide no feedback and not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Bodyguard),
    )
    val (finalBoard, history, feedback, reveals) = playGame(board, List("", "", ""))

    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    reveals shouldBe (empty)

    filterRecords(history) shouldBe empty

  }

  it should "not influence voting if the bodyguard's target is not killed" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Bodyguard),
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
      playerCards = List(Villager, Villager, Villager, Bodyguard),
    )
    val votes = votals(0 -> 3, 1 -> 2, 2 -> 1, 3 -> 0)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List()) (after being unordered)
    deaths.`protected` should equal (List()) (after being unordered)

    history.toList shouldBe empty

  }

  it should "protect the target and take out the second majority if the bodyguard's target is killed" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Bodyguard),
    )
    val votes = votals(0 -> 1, 1 -> 0, 2 -> 0, 3 -> 1, 4 -> 0)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(1)) (after being unordered)
    deaths.`protected` should equal (List(0)) (after being unordered)

    history.toVector should have length (2)
    history.toVector(0).displayText(SampleUserMapping(5)) should include (mockName(0)) // Protection message
    history.toVector(1).displayText(SampleUserMapping(5)) should include (mockName(1)) // New death message

  }

  it should "protect the target and take out the second majority if a paranormal investigator looking at a bodyguard has their target killed" in {
    // PIs don't copy bodyguards in actual gameplay, since bodyguards
    // are town-aligned. But the interaction is, according to the
    // code, well-defined, so we're going to test it.
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, ParanormalInvestigator, Bodyguard),
    )
    board(id(4)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Bodyguard.createInstance(SampleUserMapping(6), Some(id(4))))

    val votes = votals(0 -> 1, 1 -> 0, 2 -> 0, 3 -> 1, 4 -> 0, 5 -> 2)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(1)) (after being unordered)
    deaths.`protected` should equal (List(0)) (after being unordered)

    history.toVector should have length (2)
    history.toVector(0).displayText(SampleUserMapping(6)) should include (mockName(0)) // Protection message
    history.toVector(1).displayText(SampleUserMapping(6)) should include (mockName(1)) // New death message

  }

  it should "protect the target (and kill the second majority) if the bodyguard's target is killed in a split vote" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Villager, Villager, Villager, Bodyguard),
    )
    val votes = votals(0 -> 4, 1 -> 4, 2 -> 4, 3 -> 2, 4 -> 2, 5 -> 0, 6 -> 0, 7 -> 2)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(0, 4)) (after being unordered)
    deaths.`protected` should equal (List(2)) (after being unordered)

    history.toVector should have length (2)
    history.toVector(0).displayText(SampleUserMapping(8)) should include (mockName(2)) // Protection message
    history.toVector(1).displayText(SampleUserMapping(8)) should include (mockName(0)) // New death message

  }

  it should "not kill a second majority if there is no second majority" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Bodyguard),
    )
    val votes = votals(0 -> 1, 1 -> 0, 2 -> 0, 3 -> 0, 4 -> 0)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List()) (after being unordered)
    deaths.`protected` should equal (List(0)) (after being unordered)

    history.toVector should have length (1)
    history.toVector(0).displayText(SampleUserMapping(5)) should include (mockName(0))

  }

  it should "not kill a third majority" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Villager, Villager, Villager, Bodyguard, Bodyguard),
    )
    val votes = votals(0 -> 2, 1 -> 2, 2 -> 1, 3 -> 1, 4 -> 0, 5 -> 0, 6 -> 0, 7 -> 0, 8 -> 1)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List()) (after being unordered)
    deaths.`protected` should equal (List(0, 1)) (after being unordered)

    history.toVector should have length (3)
    history.toVector(0).displayText(SampleUserMapping(9)) should include (mockName(0)) // 0 is protected
    history.toVector(1).displayText(SampleUserMapping(9)) should include (mockName(1)) // second majority dies
    history.toVector(2).displayText(SampleUserMapping(9)) should include (mockName(1)) // 1 is protected

  }

  it should "protect the hunter's target if the hunter is killed" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Villager, Hunter, Bodyguard),
    )
    val votes = votals(0 -> 5, 1 -> 5, 2 -> 5, 3 -> 0, 4 -> 0, 5 -> 1, 6 -> 1)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(5)) (after being unordered)
    deaths.`protected` should equal (List(1)) (after being unordered)

    history.toVector should have length (2)
    history.toVector(0).displayText(SampleUserMapping(7)) should include (mockName(1)) // Hunter kills 1
    history.toVector(1).displayText(SampleUserMapping(7)) should include (mockName(1)) // Bodyguard protects 1

  }

  it should "block a hunter kill if the hunter is protected" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Villager, Hunter, Bodyguard),
    )
    val votes = votals(0 -> 5, 1 -> 5, 2 -> 5, 3 -> 5, 4 -> 5, 5 -> 0, 6 -> 5)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List()) (after being unordered)
    deaths.`protected` should equal (List(5)) (after being unordered)

    history.toVector should have length (1)
    history.toVector(0).displayText(SampleUserMapping(7)) should include (mockName(5)) // Bodyguard protects 5

  }

  it should "act independently of the hunter in case of unrelated targets" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Villager, Villager, Hunter, Bodyguard, Villager, Villager),
    )
    val votes = votals(0 -> 4, 1 -> 4, 2 -> 9, 3 -> 6, 4 -> 6, 5 -> 6, 6 -> 0, 7 -> 4, 8 -> 2, 9 -> 2)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(0, 2, 6)) (after being unordered)
    deaths.`protected` should equal (List(4)) (after being unordered)

    history.toVector should have length (3)
    history.toVector(0).displayText(SampleUserMapping(7)) should include (mockName(4)) // Bodyguard protects 4
    history.toVector(1).displayText(SampleUserMapping(7)) should include (mockName(2)) // Second majority
    history.toVector(2).displayText(SampleUserMapping(7)) should include (mockName(0)) // Hunter kills 0

  }

  it should "protect the target and take out the second majority if a copy-bodyguard's target is killed" in {
    val board = createBoard(
      left = Werewolf,
      middle = Bodyguard,
      right = Tanner,
      playerCards = List(Villager, Villager, Villager, Villager, Copycat),
    )

    board(id(4)).asInstanceOf[Copycat.Instance].copiedRole =
      Some(Bodyguard.createInstance(SampleUserMapping(5), Some(id(4))))

    val votes = votals(0 -> 1, 1 -> 0, 2 -> 0, 3 -> 1, 4 -> 0)
    val (deaths, history) = runVotes(board, votes)

    deaths.dead should equal (List(1)) (after being unordered)
    deaths.`protected` should equal (List(0)) (after being unordered)

    history.toVector should have length (2)
    history.toVector(0).displayText(SampleUserMapping(5)) should include (mockName(0)) // Protection message
    history.toVector(1).displayText(SampleUserMapping(5)) should include (mockName(1)) // New death message

  }

}
