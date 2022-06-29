
package com.mercerenies.werewolf
package game
package role
package wincon

import id.Id
import board.Endgame
import role.instance.RoleInstance

import org.javacord.api.entity.user.User

import scala.annotation.tailrec

trait WinCondition {

  // Note: Name is just for debugging purposes and is never shown to
  // the user.
  def name: String

  def blurb: String

  // Used if a wincon-copying role (like the Paranormal Investigator)
  // ends up with this win condition. This text will be shown in the
  // game summary for the given role.
  def snapshotSummary: String

  // For evaluating win conditions, those with a higher precedence
  // evaluate first. If a win condition (such as Tanner) triggers a
  // HardWin, then all wincons with a lower precedence automatically
  // lose.
  def precedence: Int

  def determineOutcome(endgame: Endgame, user: Id[User]): Outcome

}

object WinCondition {

  // I make no guarantees about the order of the returned values.
  def determineWinners(endgame: Endgame): Seq[Id[User]] = {
    val groupedWincons: Map[Int, List[(Id[User], RoleInstance)]] =
      endgame.board.playerRoleInstances.groupBy { (_, instance) => instance.winCondition.precedence }
    val precedences = groupedWincons.keys.toList.sortBy { - _ }
    val calculator = WinnerCalculator(endgame, groupedWincons)
    calculator.calculate(precedences, Nil)
  }

  private class WinnerCalculator(
    val endgame: Endgame,
    val groupedWincons: Map[Int, List[(Id[User], RoleInstance)]],
  ) {

    // Takes the remaining precedences (in descending order) and a
    // list of the winners found so far.
    @tailrec
    final def calculate(precedences: List[Int], winnersAcc: List[Id[User]]): List[Id[User]] =
      precedences match {
        case Nil => winnersAcc
        case (x :: xs) => {
          val wincons = groupedWincons(x)
          val outcomes: List[(Id[User], Outcome)] = wincons.map { (userId, instance) =>
            (userId, instance.winCondition.determineOutcome(endgame, userId))
          }
          val newWinners = outcomes.filter { (_, outcome) => outcome.isWin }.map { (userId, _) => userId }
          val anyHardOutcomes = outcomes.exists { (_, outcome) => outcome.isHard }
          if (anyHardOutcomes) {
            // There's been a hard result, so we have the full list of
            // winners; stop computing.
            newWinners ++ winnersAcc
          } else {
            // Continue recursing
            calculate(xs, newWinners ++ winnersAcc)
          }
        }
      }

  }

}
