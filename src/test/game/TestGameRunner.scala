
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.Role
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, BoardTestUtil}
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
    playerActions: List[String],
  ): (Board, Map[Id[User], FeedbackMessage]) = {

    val playerIds = playerActions.indices.map(id).toList

    // Now send out all of the night actions (this mutates the
    // RoleInstance objects in the board)
    playerIds zip playerActions foreach { (playerId, nightAction) =>
      val nightHandler = board(playerId).nightHandler
      nightHandler.onDirectMessage(nightAction)
    }

    // Once all night actions have been collected, run the night
    // phase and collect feedback objects.
    val NightPhaseResult(finalBoard, _, responses) = NightPhaseEvaluator.evaluate(board, playerIds, RecordedGameHistory.empty)
    (finalBoard, responses.toMap)
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
  ): (Board, Map[Id[User], FeedbackMessage]) = {
    val initialBoard = BoardTestUtil.createBoard(left, middle, right, players.map(_._1))
    playGame(initialBoard, players.map(_._2))
  }

}
