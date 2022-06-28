
package com.mercerenies.werewolf
package game
package votes

import board.{Board, PlayerOrder}
import record.RecordedGameHistory
import id.Id
import context.{VotesContextResult, VotesContext}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object VotingEvaluator {

  // Evaluate who should die, given the roles in play and the player
  // votes.
  def evaluate(board: Board, order: PlayerOrder, votes: Votals[Id[User]]): (DeathRoster[Id[User]], RecordedGameHistory) = {
    val initialKills = votes.majority.map { (_, DeathStatus.Dead) }
    val initialRoster: DeathRoster[Id[User]] = DeathRoster.from(votes).updatedAll(initialKills)

    // Assuming all of the roles in the role list satisfy the
    // conditions listed in the documentation of votePhaseAction, this
    // process will terminate in at most N + 1 steps, where N is the
    // number of players.
    //
    // Proof: Suppose we have run oneIteration N times. I claim that
    // the (N + 1)th step will change nothing. First, each of the
    // first N iterations must have changed the death roster, because
    // if one of them failed to then the process would have already
    // stopped earlier. Second, each of the iterations must have had a
    // *different* role alter the death roster, because the same role
    // cannot have done so twice by the assumptions on
    // votePhaseAction. Thus, every role has already acted. So there
    // is no role which can modify the death roster in the (N + 1)th
    // step. Thus, the process terminates.
    //
    // Q.E.D
    val action = allIterations(board, order, order.length + 1)
    val VotesContextResult(finalRoster, history, _) = action.run(votes, initialRoster, RecordedGameHistory.empty)

    (finalRoster, history)
  }

  private def allIterations(board: Board, order: PlayerOrder, n: Int): VotesContext[Boolean] =
    if (n <= 0) {
      false.point
    } else {
      oneIteration(board, order) >>= {
        case false => {
          // Nothing changed, so stop iterating
          false.point
        }
        case true => {
          // Something changed, so run again
          allIterations(board, order, n - 1)
        }
      }
    }

  private def oneIteration(board: Board, order: PlayerOrder): VotesContext[Boolean] =
    order.toList.traverse { playerId =>
      board(playerId).votePhaseAction(playerId)
    }.map { xs => xs.foldLeft(false) { _ || _ } }

}
