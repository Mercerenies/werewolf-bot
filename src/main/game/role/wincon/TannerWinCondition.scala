
package com.mercerenies.werewolf
package game
package role
package wincon

import id.Id
import board.Endgame

import org.javacord.api.entity.user.User

case object TannerWinCondition extends WinCondition {

  override val name: String =
    "Tanner Win Condition"

  override val blurb: String =
    "Your goal is to die. You win if you are one of the players who is killed."

  override val precedence: Int =
    WinPrecedence.TANNER

  def determineOutcome(endgame: Endgame, user: Id[User]): Outcome = {
    // Tanner wins if he dies. A tanner is always a hard win. (TODO
    // Waiting for feedback on whether that interpretation of the
    // rules is correct)
    Outcome.hardWin(endgame.deaths.contains(user))
  }

}
