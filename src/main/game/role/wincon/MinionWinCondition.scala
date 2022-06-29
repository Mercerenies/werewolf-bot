
package com.mercerenies.werewolf
package game
package role
package wincon

import id.Id
import board.Endgame
import util.TextDecorator.*

import org.javacord.api.entity.user.User

// Note carefully: The official One Night rules state that a minion
// without werewolves must get someone killed to win. In that
// situation, a town win is almost impossible (hence the minion has a
// free win), so I'm using a common house rule here: If there is a
// minion and *no* werewolves, then the minion simply adopts the
// town's win condition.
case object MinionWinCondition extends WinCondition {

  override val name: String =
    "Minion Win Condition"

  override val blurb: String =
    "You win if a Werewolf is " + italic("not") + " killed. If there are no werewolves, you win if no one dies."

  override val snapshotSummary: String =
    "Minion"

  override val precedence: Int =
    WinPrecedence.WEREWOLF_AND_MINION

  def determineOutcome(endgame: Endgame, user: Id[User]): Outcome = {
    // If there are no werewolves, then the minion wins with the
    // town. Otherwise, minion wins with the werewolf team.
    if (endgame.werewolves.isEmpty) {
      TownWinCondition.determineOutcome(endgame, user)
    } else {
      WerewolfWinCondition.determineOutcome(endgame, user)
    }
  }

}
