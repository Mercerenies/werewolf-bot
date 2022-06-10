
package com.mercerenies.werewolf
package state
package properties

import id.Id

import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.ServerTextChannel

trait GameProperties {
  val channelId: Id[ServerTextChannel]
  val hostId: Id[User]

  val nightPhaseLength: TimePeriod

  val dayPhaseLength: TimePeriod

  // How long into the night to send a reminder to players who have
  // not responded. Should be None if no reminder should be sent.
  val nightPhaseReminderTime: Option[TimePeriod]

}
