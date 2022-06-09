
package com.mercerenies.werewolf
package state
package properties

import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel

case class DefaultGameProperties(
  override val channel: TextChannel & Nameable,
  override val host: User,
) extends GameProperties {

  override val nightPhaseLength: TimePeriod = TimePeriod.hours(48)

  override val nightPhaseReminderTime: Option[TimePeriod] = Some(TimePeriod.hours(24))

}
