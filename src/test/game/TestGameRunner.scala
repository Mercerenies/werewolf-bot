
package com.mercerenies.werewolf
package game

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import role.Role
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, BoardTestUtil}

object TestGameRunner {

  // Returns a pre-determined name and a mock User object for any
  // user.
  object SampleUserMapping extends UserMapping {

    def get(id: Id[User]): Option[User] =
      Some(MockitoSugar.mock[User])

    def getName(id: Id[User]): Option[String] =
      Some(mockName(id.toLong))

  }

  def mockName(n: Long): String =
    s"<User ${n}>"

  // Just to help with type inference since we only use Id[User] here.

  private def id(x: Long): Id[User] =
    Id.fromLong(x)

  private def id(x: Int): Id[User] =
    Id.fromLong(x)

  // Game simulator. Takes center cards and a list of players given by
  // (1) the role assigned to that player, and (2) the player's night
  // action text. Returns the final board state and a response to each
  // player.
  def playGame(
    left: Role,
    middle: Role,
    right: Role,
    players: List[(Role, String)],
  ): (Board, List[FeedbackMessage]) = {

    val playerIds = players.indices.map(id).toList
    val nightActions = players.map(_._2)
    val initialBoard = BoardTestUtil.createBoard(left, middle, right, players.map(_._1))

    // Now send out all of the night actions (this mutates the
    // RoleInstance objects in the board)
    playerIds zip nightActions foreach { (playerId, nightAction) =>
      val nightHandler = initialBoard(playerId).nightHandler
      nightHandler.onDirectMessage(nightAction)
    }

    // Once all night actions have been collected, run the night
    // phase and collect feedback objects.
    val (finalBoard, responses) = NightPhaseState.evaluateNightPhase(SampleUserMapping, initialBoard)
    (finalBoard, responses.map(_._2))
  }

}
