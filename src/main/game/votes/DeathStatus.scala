
package com.mercerenies.werewolf
package game
package votes

import scala.math.Ordering

enum DeathStatus {
  case Alive
  case Dead
  case Protected
}

object DeathStatus {

  def minValue: DeathStatus = DeathStatus.Alive
  def maxValue: DeathStatus = DeathStatus.Protected

  given DeathStatusIsOrdering : Ordering[DeathStatus] =
    Ordering.by { _.ordinal }

}
