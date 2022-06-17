
package com.mercerenies.werewolf
package game
package record

import id.UserMapping
import util.html.HtmlFragment
import board.snapshot.RoleSnapshot

// A GameRecord is an event that happened during the game (often but
// not always during the night phase) and which should be recorded and
// shown to the players at the very end.
trait GameRecord {

  // Plaintext record of what's happened
  def displayText(userMapping: UserMapping): String

  // HTML record (as a <li>) of what's happened
  def htmlText(userMapping: UserMapping)(using HtmlFragment): Unit

  // Any records of the form "Player A with role X has performed
  // action" should be updated to say "Player A with role fn(X) has
  // performed action". For all other records, the default
  // implementation (which does nothing and returns `this`) should
  // suffice.
  //
  // This allows copying roles such as the Doppelganger to provide
  // better feedback. A doppel-seer will run the seer's usual role
  // instance, and then the doppelganger can apply mapPerformer to
  // change the subject to "Doppelganger (Seer)" or similar.
  def mapActor(fn: (RoleSnapshot) => RoleSnapshot): GameRecord =
    this

}
