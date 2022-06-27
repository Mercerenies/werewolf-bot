
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

  override val snapshotSummary: String =
    "Tanner"

  override val precedence: Int =
    WinPrecedence.TOWN_AND_TANNER

  def determineOutcome(endgame: Endgame, user: Id[User]): Outcome = {
    // Tanner wins if he dies. A tanner is always a hard win, which is
    // of the same precedence as town but precedes werewolves.
    Outcome.hardWin(endgame.deaths.contains(user))
  }

}
