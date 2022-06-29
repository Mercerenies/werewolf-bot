
package com.mercerenies.werewolf
package game
package role
package wincon

import id.Id
import util.TextDecorator.*
import board.Endgame

import org.javacord.api.entity.user.User

case object WerewolfWinCondition extends WinCondition {

  override val name: String =
    "Werewolf Win Condition"

  override val blurb: String =
    "You win if a Werewolf is " + italic("not") + " killed."

  override val snapshotSummary: String =
    "Werewolf"

  override val precedence: Int =
    WinPrecedence.WEREWOLF_AND_MINION

  def determineOutcome(endgame: Endgame, user: Id[User]): Outcome = {
    // Werewolves win as long as none of the dead are werewolves.
    // Werewolf win is soft (but that shouldn't matter since
    // werewolves have the lowest precedence wincon in the game)
    val satisfied = !endgame.anyWerewolvesDied
    Outcome.softWin(satisfied)
  }

}
