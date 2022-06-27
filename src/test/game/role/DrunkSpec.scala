
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class DrunkSpec extends GameplayUnitSpec {

  "The Drunk role" should "swap with the designated card, without being told what it is" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Drunk, Villager, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("right", "", ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Drunk)
    finalBoard(id(0)).role should be (Tanner)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Villager)

    feedback(id(0)).mkString should not include (mockName(0))
    feedback(id(0)).mkString should not include (mockName(2))
    feedback(id(0)).mkString should not include regex ("(?i)robber|villager|werewolf|tanner")
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)right")

  }

  it should "default to the left card if no feedback is received" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Drunk, Villager, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))

    finalBoard(TablePosition.Left).role should be (Drunk)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (Villager)

    feedback(id(0)).mkString should not include regex ("(?i)villager|robber|werewolf|tanner")
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)left")

  }

  it should "act after the troublemaker" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Drunk, Tanner, Tanner, Troublemaker),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("middle", "", "", mockName(0) + " " + mockName(1)))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Tanner)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Drunk)
    finalBoard(id(2)).role should be (Tanner)
    finalBoard(id(3)).role should be (Troublemaker)

  }

  it should "act after the robber" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Drunk, Tanner, Tanner, Robber),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("right", "", "", mockName(0)))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Robber)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Tanner)
    finalBoard(id(2)).role should be (Tanner)
    finalBoard(id(3)).role should be (Drunk)

  }

  it should "act after the robber and the troublemaker" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Drunk, Tanner, Troublemaker, Robber),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("right", "", mockName(0) + " " + mockName(1), mockName(0)))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Villager)
    finalBoard(id(1)).role should be (Robber)
    finalBoard(id(2)).role should be (Troublemaker)
    finalBoard(id(3)).role should be (Drunk)

  }

}
