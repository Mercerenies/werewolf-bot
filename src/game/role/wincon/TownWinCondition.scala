
package com.mercerenies.werewolf
package game
package role
package wincon

case object TownWinCondition extends WinCondition {

  override val name: String =
    "Town Win Condition"

  override val blurb: String =
    "You win if a Werewolf is killed. If there are no Werewolves, then you win if no one dies."

}
