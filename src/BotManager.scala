
package com.mercerenies.werewolf

import org.javacord.api.DiscordApi

import scala.concurrent.{Future, ExecutionContext}
import scala.jdk.FutureConverters.*

final class BotManager(
  private val config: BotConfig,
  private val api: DiscordApi,
)(
  using ExecutionContext
) {

}

object BotManager {

  def initialize(config: BotConfig)(using ExecutionContext): Future[BotManager] =
    config.produceApi().map { new BotManager(config, _) }

}
