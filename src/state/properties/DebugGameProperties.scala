
package com.mercerenies.werewolf
package state
package properties

import id.Id

import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel

// A game with a very short night phase, for debugging purposes.
case class DebugGameProperties(
  override val channelId: Id[TextChannel & Nameable],
  override val hostId: Id[User],
) extends GameProperties {

  override val nightPhaseLength: TimePeriod = TimePeriod.seconds(20)

  override val nightPhaseReminderTime: Option[TimePeriod] = Some(TimePeriod.seconds(10))

}