
package com.mercerenies.werewolf
package state
package properties

import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel

trait GameProperties {
  val channel: TextChannel & Nameable
  val host: User

  val nightPhaseLength: TimePeriod

  // How long into the night to send a reminder to players who have
  // not responded. Should be None if no reminder should be sent.
  val nightPhaseReminderTime: Option[TimePeriod]

}
