
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

case class DefaultGameProperties(
  override val channelId: Id[ServerTextChannel],
  override val hostId: Id[User],
) extends GameProperties {

  override val duskPhaseLength: TimePeriod = TimePeriod.hours(24)

  override val duskPhaseReminderTime: Option[TimePeriod] = Some(TimePeriod.hours(12))

  override val nightPhaseLength: TimePeriod = TimePeriod.hours(24)

  override val nightPhaseReminderTime: Option[TimePeriod] = Some(TimePeriod.hours(12))

  override val dayPhaseLength: TimePeriod = TimePeriod.hours(72)

  override val votePhaseLength: TimePeriod = TimePeriod.hours(24)

  def recordExporter(api: DiscordApi): RecordExporter = {
    val channel = api.getServerTextChannel(channelId)
    CompositeExporter(
      DiscordHtmlExporter("http://mercerenies.sdf.org/wolfie", channel),
      DiscordEmbedExporter(channel),
    )
  }

  def isSelfVotingAllowed: Boolean = false

}
