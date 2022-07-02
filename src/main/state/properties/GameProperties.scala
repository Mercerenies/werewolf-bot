
package com.mercerenies.werewolf
package state
package properties

import id.Id
import game.NightPhase
import game.record.exporter.RecordExporter

import org.javacord.api.DiscordApi
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.ServerTextChannel

import scala.concurrent.{Future, ExecutionContext}

trait GameProperties {
  val channelId: Id[ServerTextChannel]
  val hostId: Id[User]

  val duskPhaseLength: TimePeriod

  // How long into the dusk to send a reminder to players who have
  // not responded. Should be None if no reminder should be sent.
  val duskPhaseReminderTime: Option[TimePeriod]

  val nightPhaseLength: TimePeriod

  // How long into the night to send a reminder to players who have
  // not responded. Should be None if no reminder should be sent.
  val nightPhaseReminderTime: Option[TimePeriod]

  final def nighttimeLength(phase: NightPhase): TimePeriod =
    phase match {
      case NightPhase.Dusk => duskPhaseLength
      case NightPhase.Night => nightPhaseLength
    }

  final def nighttimeReminderTime(phase: NightPhase): Option[TimePeriod] =
    phase match {
      case NightPhase.Dusk => duskPhaseReminderTime
      case NightPhase.Night => nightPhaseReminderTime
    }

  val dayPhaseLength: TimePeriod

  val votePhaseLength: TimePeriod

  def recordExporter(api: DiscordApi): RecordExporter

  // Normally, self-voting is forbidden. In the debug instance, I want
  // it available so I can test using a one-player game.
  def isSelfVotingAllowed: Boolean

}
