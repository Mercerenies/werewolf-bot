
package com.mercerenies.werewolf
package game
package role
package wincon

import id.Id
import board.Endgame

import org.javacord.api.entity.user.User

trait WinCondition {

  // Note: Name is just for debugging purposes and is never shown to
  // the user.
  def name: String

  def blurb: String

  // For evaluating win conditions, those with a higher precedence
  // evaluate first. If a win condition (such as Tanner) triggers a
  // HardWin, then all wincons with a lower precedence automatically
  // lose.
  def precedence: Int

  def determineOutcome(endgame: Endgame, user: Id[User]): Outcome

}
