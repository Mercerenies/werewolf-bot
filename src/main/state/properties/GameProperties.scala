
package com.mercerenies.werewolf
package state
package properties

import id.Id
import game.record.exporter.RecordExporter

import org.javacord.api.DiscordApi
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.ServerTextChannel

import scala.concurrent.{Future, ExecutionContext}

trait GameProperties {
  val channelId: Id[ServerTextChannel]
  val hostId: Id[User]

  val nightPhaseLength: TimePeriod

  val dayPhaseLength: TimePeriod

  val votePhaseLength: TimePeriod

  // How long into the night to send a reminder to players who have
  // not responded. Should be None if no reminder should be sent.
  val nightPhaseReminderTime: Option[TimePeriod]

  def recordExporter(api: DiscordApi): RecordExporter

}
