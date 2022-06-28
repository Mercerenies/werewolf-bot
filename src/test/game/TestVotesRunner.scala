
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
import votes.{VotingEvaluator, Votals, DeathRoster}

object TestVotesRunner {

  export BoardTestUtil.{SampleUserMapping, mockName}

  // Just to help with type inference since we only use Id[User] here.

  private def id(x: Long): Id[User] =
    Id.fromLong(x)

  private def id(x: Int): Id[User] =
    Id.fromLong(x)

  def runVotes(
    board: Board,
    votes: Votals[Id[User]],
  ): (DeathRoster[Id[User]], RecordedGameHistory) = {
    val playerIds = board.players.toList.sortBy { _.toLong }
    val playerOrder = PlayerOrder(playerIds)
    VotingEvaluator.evaluate(board, playerOrder, votes)
  }

}
