
package com.mercerenies.werewolf
package game
package role
package wincon

import id.Id
import board.Endgame

import org.javacord.api.entity.user.User

case object TownWinCondition extends WinCondition {

  override val name: String =
    "Town Win Condition"

  override val blurb: String =
    "You win if a Werewolf is killed. If there are no Werewolves, then you win if no one dies."

  override val snapshotSummary: String =
    "Town"

  override val precedence: Int =
    WinPrecedence.TOWN_AND_TANNER

  def determineOutcome(endgame: Endgame, user: Id[User]): Outcome = {
    // Town wins if either
    //
    // (1) At least one werewolf died, or
    //
    // (2) There are no werewolf-identifying players and no one died
    //
    // A town win is always hard and prevents the werewolf team from
    // winning.
    if (endgame.anyWerewolvesDied) {
      // Soft win: Allow Fluffy/Ripper to do their thing.
      Outcome.SoftWin
    } else if (!endgame.anyoneDied && endgame.werewolves.isEmpty) {
      // Soft win: Allow minions to win (there are no werewolves, so
      // WerewolfWinCondition is vacuous).
      Outcome.SoftWin
    } else {
      Outcome.SoftLoss
    }
  }

}
