
package com.mercerenies.werewolf
package state
package properties

import id.Id
import id.Ids.*
import game.record.exporter.{RecordExporter, CompositeExporter, DiscordHtmlExporter, DiscordEmbedExporter}

import org.javacord.api.DiscordApi
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.ServerTextChannel

import scala.concurrent.{Future, ExecutionContext}

// A game with a very short night phase, for debugging purposes.
case class DebugGameProperties(
  override val channelId: Id[ServerTextChannel],
  override val hostId: Id[User],
) extends GameProperties {

  override val nightPhaseLength: TimePeriod = TimePeriod.seconds(20)

  override val dayPhaseLength: TimePeriod = TimePeriod.seconds(30)

  override val votePhaseLength: TimePeriod = TimePeriod.seconds(20)

  override val nightPhaseReminderTime: Option[TimePeriod] = Some(TimePeriod.seconds(10))

  def recordExporter(api: DiscordApi): RecordExporter = {
    val channel = api.getServerTextChannel(channelId)
    CompositeExporter(
      DiscordHtmlExporter("http://mercerenies.sdf.org/wolfie-test", channel),
      DiscordEmbedExporter(channel),
    )
  }

  def isSelfVotingAllowed: Boolean = true

}
