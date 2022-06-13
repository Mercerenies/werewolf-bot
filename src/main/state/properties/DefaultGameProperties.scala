
package com.mercerenies.werewolf
package state
package properties

import id.Id

import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.ServerTextChannel

case class DefaultGameProperties(
  override val channelId: Id[ServerTextChannel],
  override val hostId: Id[User],
) extends GameProperties {

  override val nightPhaseLength: TimePeriod = TimePeriod.hours(48)

  override val dayPhaseLength: TimePeriod = TimePeriod.hours(72)

  override val votePhaseLength: TimePeriod = TimePeriod.hours(24)

  override val nightPhaseReminderTime: Option[TimePeriod] = Some(TimePeriod.hours(24))

}
