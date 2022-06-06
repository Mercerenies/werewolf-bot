
package com.mercerenies.werewolf
package game
package role
package wincon

import util.TextDecorator.*

case object WerewolfWinCondition extends WinCondition {

  override val name: String =
    "Werewolf Win Condition"

  override val blurb: String =
    "You win if a Werewolf is " + italic("not") + " killed."

}
