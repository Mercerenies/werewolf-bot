
package com.mercerenies.werewolf
package game
package role
package wincon

import id.Id
import board.{Endgame, Board}

import org.javacord.api.entity.user.User

case class FluffyRipperWinCondition(
  val owner: Role,
  val target: GroupedRoleIdentity,
) extends WinCondition {

  override val name: String =
    s"${owner.name} Win Condition"

  override val blurb: String =
    s"Your goal is to see ${target.name} die. You win if ${target.name} is one of the players killed. If nobody is ${target.name}, then you win with the werewolves."

  override val snapshotSummary: String =
    target.name

  override val precedence: Int =
    WinPrecedence.WEREWOLF_AND_MINION

  def determineOutcome(endgame: Endgame, user: Id[User]): Outcome = {
    val targets = findTargets(endgame.board)
    if (targets.isEmpty) {
      // If there are no valid targets, then Fluffy / Ripper adopt the
      // werewolf win condition.
      WerewolfWinCondition.determineOutcome(endgame, user)
    } else {
      // If there are valid targets, at least one of them must have
      // died.
      Outcome.softWin(targets.exists { endgame.isDead(_) })
    }
  }

  private def findTargets(board: Board): List[Id[User]] =
    // Note: There can be multiple, if for example Ripper was copied
    // by a Doppelganger or was viewed by a Paranormal Investigator.
    board.players.filter { playerId => board(playerId).seenAs.contains(target) }

}
