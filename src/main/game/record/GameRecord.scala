
package com.mercerenies.werewolf
package game
package record

import id.UserMapping

// A GameRecord is an event that happened during the game (often but
// not always during the night phase) and which should be recorded and
// shown to the players at the very end.
trait GameRecord {

  def displayText(userMapping: UserMapping): String

}
