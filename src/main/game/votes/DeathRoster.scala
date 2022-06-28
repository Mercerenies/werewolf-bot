
package com.mercerenies.werewolf
package game
package votes

import scala.math.Ordering.Implicits.*

case class DeathRoster[A](
  val deaths: Map[A, DeathStatus],
) {

  export deaths.{apply, get}

  // Updates the status of the given player to the maximum of the
  // current status or the new one. If the current status is already
  // larger, then the value is not updated.
  def updated(value: A, deathStatus: DeathStatus): DeathRoster[A] = {
    val currentStatus = deaths.getOrElse(value, DeathStatus.minValue)
    DeathRoster(deaths + ((value, currentStatus max deathStatus)))
  }

}

object DeathRoster {

  def from[A](iter: Iterable[A]): DeathRoster[A] =
    DeathRoster(iter.map { (_, DeathStatus.Alive) }.toMap)

  def from[A](votals: Votals[A]): DeathRoster[A] =
    from(votals.players)

}
