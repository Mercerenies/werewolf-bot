
package com.mercerenies.werewolf
package game

// Miscellaneous constants and things defining rules of the game.
object Rules {

  val centerCards: Int = 3

  def rolesNeeded(playerCount: Int): Int = playerCount + centerCards

}
