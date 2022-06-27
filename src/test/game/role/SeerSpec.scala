
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class SeerSpec extends GameplayUnitSpec {

  "The Seer role" should "not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Werewolf, Seer, Werewolf),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
    reveals shouldBe empty
  }

  it should "provide feedback to a seer who looks in the middle" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "right middle", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex "(?i)middle.*werewolf"
    feedback(id(1)).mkString should include regex "(?i)right.*tanner"
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "record the action of a seer who looks in the middle" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Villager),
    )
    val (_, history, _, _) = playGame(board, List("", "right middle", ""))

    val filtered = filterRecords(history)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include regex "(?i)right.*tanner"
    filtered(0).displayText(SampleUserMapping(3)) should include regex "(?i)middle.*werewolf"
  }

  it should "provide empty feedback to a seer who does not look" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Seer),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "none"))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include regex ("(?i)werewolf|villager|tanner")
    feedback(id(2)).mkString should not include regex ("(?i)werewolf|villager|tanner")

  }

  it should "record the inaction of a seer who does not look" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Villager),
    )
    val (_, history, _, _) = playGame(board, List("", "none", ""))

    val filtered = filterRecords(history)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should not include regex ("(?i)werewolf|villager|tanner|left|middle|right")
  }

  it should "provide feedback to a seer who looks at another player's card" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", mockName(2), ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include regex "(?i)tanner"
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "record the action of a seer who looks at another player's card" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Villager, Seer, Tanner),
    )
    val (_, history, _, _) = playGame(board, List("", mockName(2), ""))

    val filtered = filterRecords(history)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(2))
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)tanner")

  }

  it should "observe the player's cards from before a troublemaker acts" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Troublemaker, Seer, Tanner, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List(s"${mockName(2)} ${mockName(3)}", mockName(2), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Troublemaker)
    finalBoard(id(1)).role should be (Seer)
    finalBoard(id(2)).role should be (Villager)
    finalBoard(id(3)).role should be (Tanner)

    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include regex "(?i)tanner"
    feedback(id(2)) should be (FeedbackMessage.none)
    feedback(id(3)) should be (FeedbackMessage.none)

  }

  it should "observe the player's cards from before a robber acts" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Robber, Seer, Tanner, Villager),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List(mockName(2), mockName(2), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Tanner)
    finalBoard(id(1)).role should be (Seer)
    finalBoard(id(2)).role should be (Robber)
    finalBoard(id(3)).role should be (Villager)

    feedback(id(0)).mkString should include (mockName(2))
    feedback(id(0)).mkString should include regex "(?i)tanner"
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include regex "(?i)tanner"
    feedback(id(2)) should be (FeedbackMessage.none)
    feedback(id(3)) should be (FeedbackMessage.none)

  }

  it should "see the original cards correctly even if the seer card ends up swapped by troublemaker" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Troublemaker, Seer, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List(s"${mockName(1)} ${mockName(2)}", mockName(2), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Troublemaker)
    finalBoard(id(1)).role should be (Tanner)
    finalBoard(id(2)).role should be (Seer)

    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include regex "(?i)tanner"
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "see the original cards correctly even if the seer card ends up swapped by robber" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Robber, Seer, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List(mockName(1), mockName(0), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Seer)
    finalBoard(id(1)).role should be (Robber)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(0)).mkString should include (mockName(1))
    feedback(id(0)).mkString should include regex "(?i)seer"
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex "(?i)robber"
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "see the original cards correctly even if observing a drunk" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Drunk, Seer, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("middle", mockName(0), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Drunk)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Seer)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(0)).mkString should include regex ("(?i)middle")
    feedback(id(0)).mkString should not include regex ("(?i)werewolf|villager|tanner")
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex "(?i)drunk"
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "see the original cards correctly even if observing a card which will later be swapped by a drunk" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Drunk, Seer, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("middle", "left middle", ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Drunk)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Werewolf)
    finalBoard(id(1)).role should be (Seer)
    finalBoard(id(2)).role should be (Tanner)

    feedback(id(0)).mkString should include regex ("(?i)middle")
    feedback(id(0)).mkString should not include regex ("(?i)werewolf|villager|tanner")
    feedback(id(1)).mkString should include regex "(?i)villager"
    feedback(id(1)).mkString should include regex "(?i)werewolf"
    feedback(id(2)) should be (FeedbackMessage.none)

  }

  it should "see the original player cards correctly even if observing a card which will be swapped by a witch" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Witch, Seer, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("middle " + mockName(2), "left middle", ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Tanner)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Witch)
    finalBoard(id(1)).role should be (Seer)
    finalBoard(id(2)).role should be (Werewolf)

    feedback(id(1)).mkString should include regex "(?i)villager"
    feedback(id(1)).mkString should include regex "(?i)werewolf"
    feedback(id(1)).mkString should not include regex ("(?i)tanner")

  }

  it should "see the original table cards correctly even if observing a card which will be swapped by a witch" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Witch, Seer, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("middle " + mockName(2), mockName(2), ""))

    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Tanner)
    finalBoard(TablePosition.Right).role should be (Tanner)
    finalBoard(id(0)).role should be (Witch)
    finalBoard(id(1)).role should be (Seer)
    finalBoard(id(2)).role should be (Werewolf)

    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include regex "(?i)tanner"

  }

}
