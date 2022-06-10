
package com.mercerenies.werewolf
package state
package properties

import id.Id

import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel

case class DefaultGameProperties(
  override val channelId: Id[TextChannel & Nameable],
  override val hostId: Id[User],
) extends GameProperties {

  override val nightPhaseLength: TimePeriod = TimePeriod.hours(48)

  override val dayPhaseLength: TimePeriod = TimePeriod.hours(72)

  override val nightPhaseReminderTime: Option[TimePeriod] = Some(TimePeriod.hours(24))

}
