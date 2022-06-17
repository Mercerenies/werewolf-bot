
package com.mercerenies.werewolf
package game
package record

import id.UserMapping
import util.html.HtmlFragment

// A GameRecord is an event that happened during the game (often but
// not always during the night phase) and which should be recorded and
// shown to the players at the very end.
trait GameRecord {

  // Plaintext record of what's happened
  def displayText(userMapping: UserMapping): String

  // HTML record (as a <li>) of what's happened
  def htmlText(userMapping: UserMapping)(using HtmlFragment): Unit

}
