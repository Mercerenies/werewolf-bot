
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.Role
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, BoardTestUtil, Position, PlayerOrder}
import record.RecordedGameHistory

object TestGameRunner {

  export BoardTestUtil.{SampleUserMapping, mockName}

  // Just to help with type inference since we only use Id[User] here.

  private def id(x: Long): Id[User] =
    Id.fromLong(x)

  private def id(x: Int): Id[User] =
    Id.fromLong(x)

  // Game simulator. Takes a board and list of player night DMs.
  def playGame(
    board: Board,
    playerActions: List[String | (String, String)],
  ): (Board, RecordedGameHistory, Map[Id[User], FeedbackMessage], Set[Position]) = {

    val duskActions: List[String] = playerActions.map {
      case (dusk, _) => dusk
      case _ => ""
    }

    val nightActions: List[String] = playerActions.map {
      case (_, night) => night
      case night: String => night
    }

    val playerIds = playerActions.indices.map(id).toList

    // Send out all of the dusk actions (this mutates the
    // RoleInstance objects in the board)
    playerIds zip duskActions foreach { (playerId, duskAction) =>
      val duskHandler = board(playerId).duskHandler
      duskHandler.onDirectMessage(board, duskAction)
    }

    // Run the dusk phase.
    val NightPhaseResult(initialBoard, duskHistory, duskResponses, duskRevealedCards) = NightPhaseEvaluator.evaluate(board, NightPhase.Dusk, PlayerOrder(playerIds), RecordedGameHistory.empty)
    // There are no roles that reveal cards at dusk, so assert that
    // here.
    assert(duskRevealedCards.isEmpty, s"The following cards were revealed at dusk: ${duskRevealedCards}")

    // Now send out all of the night actions (this mutates the
    // RoleInstance objects in the board)
    playerIds zip nightActions foreach { (playerId, nightAction) =>
      val nightHandler = initialBoard(playerId).nightHandler
      nightHandler.onDirectMessage(initialBoard, nightAction)
    }

    // Once all night actions have been collected, run the night
    // phase and collect feedback objects.
    val NightPhaseResult(finalBoard, history, responses, revealedCards) = NightPhaseEvaluator.evaluate(initialBoard, NightPhase.Night, PlayerOrder(playerIds), duskHistory)

    val allResponses = util.merge(duskResponses, responses) { _ ++ _ }
    (finalBoard, history, allResponses, revealedCards)
  }

  // Game simulator. Takes center cards and a list of players given by
  // (1) the role assigned to that player, and (2) the player's night
  // action text. Returns the final board state and a response to each
  // player.
  def playGame(
    left: Role,
    middle: Role,
    right: Role,
    players: List[(Role, String)],
  ): (Board, RecordedGameHistory, Map[Id[User], FeedbackMessage], Set[Position]) = {
    val initialBoard = BoardTestUtil.createBoard(left, middle, right, players.map(_._1))
    playGame(initialBoard, players.map(_._2))
  }

}
