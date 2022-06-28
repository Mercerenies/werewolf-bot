
package com.mercerenies.werewolf
package game
package votes

import scala.math.Ordering.Implicits.*

case class DeathRoster[A](
  val deaths: Map[A, DeathStatus],
) {

  export deaths.{apply, get}

  def dead: List[A] =
    deaths.filter { (_, v) => v == DeathStatus.Dead }.map { (k, _) => k }.toList

  def isDead(value: A): Boolean =
    this.get(value) == Some(DeathStatus.Dead)

  // Updates the status of the given player to the maximum of the
  // current status or the new one. If the current status is already
  // larger, then the value is not updated.
  def updated(value: A, deathStatus: DeathStatus): DeathRoster[A] = {
    val currentStatus = deaths.getOrElse(value, DeathStatus.minValue)
    DeathRoster(deaths + ((value, currentStatus max deathStatus)))
  }

  def updatedAll(values: Iterable[(A, DeathStatus)]): DeathRoster[A] =
    values.foldLeft(this) { (acc, curr) => acc.updated(curr._1, curr._2) }

}

object DeathRoster {

  def from[A](iter: Iterable[A]): DeathRoster[A] =
    DeathRoster(iter.map { (_, DeathStatus.Alive) }.toMap)

  def from[A](votals: Votals[A]): DeathRoster[A] =
    from(votals.players)

}
