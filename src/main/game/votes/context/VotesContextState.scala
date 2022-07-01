
package com.mercerenies.werewolf
package game
package votes
package context

import id.Id
import board.Board

import org.javacord.api.entity.user.User

case class VotesContextState(
  val votals: Votals[Id[User]],
  val deaths: DeathRoster[Id[User]],
  val board: Board,
)

object VotesContextState {

  def fromVotals(votals: Votals[Id[User]], board: Board): VotesContextState =
    VotesContextState(votals, DeathRoster.from(votals), board)

}
