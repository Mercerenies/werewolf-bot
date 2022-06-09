
package com.mercerenies.werewolf
package state
package properties

import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel

// A game with a very short night phase, for debugging purposes.
case class DebugGameProperties(
  override val channel: TextChannel & Nameable,
  override val host: User,
) extends GameProperties {

  override val nightPhaseLength: TimePeriod = TimePeriod.seconds(20)

  override val nightPhaseReminderTime: Option[TimePeriod] = Some(TimePeriod.seconds(10))

}
