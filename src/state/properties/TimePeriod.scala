
package com.mercerenies.werewolf
package state
package properties

import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel

import java.util.concurrent.TimeUnit

import scala.concurrent.duration.Duration

// Lightweight duration class for measuring (and pretty-printing)
// periods of time in a game.
final class TimePeriod private(private val _seconds: Long) {

  override def equals(that: Any): Boolean = that match {
    case that: TimePeriod => this._seconds == that._seconds
    case _ => false
  }

  override def hashCode: Int = ("TimePeriod", _seconds).hashCode

  // Note: Any methods more coarse than seconds may round off their
  // results to the nearest unit.

  def seconds: Long = _seconds

  def minutes: Long = seconds / 60l

  def hours: Long = seconds / (60l * 60l)

  def days: Long = seconds / (60l * 60l * 24l)

  def toDuration: Duration =
    Duration(_seconds, TimeUnit.SECONDS)

  ///// toString

}

object TimePeriod {

  def seconds(n: Long): TimePeriod =
    new TimePeriod(n)

  def minutes(n: Long): TimePeriod =
    new TimePeriod(n * 60l)

  def hours(n: Long): TimePeriod =
    new TimePeriod(n * 60l * 60l)

  def days(n: Long): TimePeriod =
    new TimePeriod(n * 60l * 60l * 24l)

}
