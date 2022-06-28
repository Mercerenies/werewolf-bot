
package com.mercerenies.werewolf
package game
package votes
package context

import id.Id

import org.javacord.api.entity.user.User

case class VotesContextState(
  val votals: Votals[Id[User]],
  val deaths: DeathRoster[Id[User]],
)

object VotesContextState {

  def fromVotals(votals: Votals[Id[User]]): VotesContextState =
    VotesContextState(votals, DeathRoster.from(votals))

}
