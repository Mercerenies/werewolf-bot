
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

class RobberSpec extends GameplayUnitSpec {

  "The Robber role" should "be included in the global role list" in {
    Role.all should contain (Robber)
  }

  it should "perform no action if a robber elects to do nothing" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Robber, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include (mockName(0))
    feedback(id(1)).mkString should not include (mockName(1))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(1)).mkString should not include regex ("(?i)robber|villager|werewolf|tanner")
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "swap roles and be given information if a robber elects to act" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Robber, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", mockName(0), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Robber)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex ("(?i)villager")
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)villager")
    filtered(0).displayText(SampleUserMapping(3)) should not include regex ("(?i)tanner|werewolf")

  }

  it should "act before the troublemaker" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Werewolf, Robber, Tanner, Troublemaker),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", mockName(0), "", mockName(0) + " " + mockName(1)))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Robber)
    finalBoard(id(2)).role should be (Villager)
    finalBoard(id(3)).role should be (Tanner)
    finalBoard(id(4)).role should be (Troublemaker)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include (mockName(0))
    feedback(id(2)).mkString should include regex ("(?i)villager")
    feedback(id(3)) should be (FeedbackMessage.none)
    feedback(id(4)).mkString should include (mockName(0))
    feedback(id(4)).mkString should include (mockName(1))
    feedback(id(4)).mkString should not include regex ("(?i)werewolf|robber|tanner|villager")

  }

  it should "take the Paranormal Investigator's win condition if it robs the Paranormal Investigator" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(ParanormalInvestigator, Werewolf, Robber),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List(mockName(1), "", mockName(0)))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Robber)
    finalBoard(id(1)).role should be (Werewolf)
    finalBoard(id(2)).role should be (ParanormalInvestigator)

    finalBoard(id(0)).winCondition should be (TownWinCondition)
    finalBoard(id(1)).winCondition should be (WerewolfWinCondition)
    finalBoard(id(2)).winCondition should be (WerewolfWinCondition)

    feedback(id(0)).mkString should include regex ("(?i)werewolf")
    feedback(id(2)).mkString should include regex ("(?i)paranormal investigator")
    feedback(id(2)).mkString should not include regex ("(?i)werewolf")

  }

  it should "swap roles and be given information if a copy-robber elects to act" in {
    val board = createBoard(
      left = Villager,
      middle = Robber,
      right = Villager,
      playerCards = List(Villager, Copycat, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", ("middle", mockName(0)), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Robber)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Copycat)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex ("(?i)villager")
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (3)
    filtered should have length (2)

    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)middle")
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)robber")

    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)villager")
    filtered(1).displayText(SampleUserMapping(3)) should not include regex ("(?i)tanner|werewolf")

  }

  it should "act before a copy-troublemaker" in {
    val board = createBoard(
      left = Villager,
      middle = Troublemaker,
      right = Villager,
      playerCards = List(Villager, Werewolf, Robber, Tanner, Copycat),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", mockName(0), "", ("middle", mockName(0) + " " + mockName(1))))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Troublemaker)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Robber)
    finalBoard(id(2)).role should be (Villager)
    finalBoard(id(3)).role should be (Tanner)
    finalBoard(id(4)).role should be (Copycat)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include (mockName(0))
    feedback(id(2)).mkString should include regex ("(?i)villager")
    feedback(id(3)) should be (FeedbackMessage.none)
    feedback(id(4)).mkString should include (mockName(0))
    feedback(id(4)).mkString should include (mockName(1))
    feedback(id(4)).mkString should not include regex ("(?i)werewolf|robber|tanner|villager")

  }

  it should "allow a copy-robber to act before the troublemaker" in {
    val board = createBoard(
      left = Villager,
      middle = Robber,
      right = Villager,
      playerCards = List(Villager, Werewolf, Copycat, Tanner, Troublemaker),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ("middle", mockName(0)), "", mockName(0) + " " + mockName(1)))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Robber)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Copycat)
    finalBoard(id(2)).role should be (Villager)
    finalBoard(id(3)).role should be (Tanner)
    finalBoard(id(4)).role should be (Troublemaker)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(2)).mkString should include (mockName(0))
    feedback(id(2)).mkString should include regex ("(?i)villager")
    feedback(id(3)) should be (FeedbackMessage.none)
    feedback(id(4)).mkString should include (mockName(0))
    feedback(id(4)).mkString should include (mockName(1))
    feedback(id(4)).mkString should not include regex ("(?i)werewolf|robber|tanner|villager")

  }

}
