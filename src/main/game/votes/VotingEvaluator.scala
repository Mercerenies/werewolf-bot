
package com.mercerenies.werewolf
package game
package votes

import board.Board
import record.RecordedGameHistory
import id.Id

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object VotingEvaluator {

  // Evaluate who should die, given the roles in play and the player
  // votes.
  def evaluate(board: Board, votes: Votals[Id[User]]): (DeathRoster[Id[User]], RecordedGameHistory) = {
    val initialKills = votes.majority.map { (_, DeathStatus.Dead) }
    val initialRoster: DeathRoster[Id[User]] = DeathRoster.from(votes).updatedAll(initialKills)
    /////
    (initialRoster, RecordedGameHistory.empty)
  }

}
