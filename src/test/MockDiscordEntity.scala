
package com.mercerenies.werewolf

import org.javacord.api.DiscordApi
import org.javacord.api.entity.DiscordEntity

import java.time.Instant

// Mock DiscordEntity which has an ID value. getApi raises an
// exception by default.
open class MockDiscordEntity(
  private val id: Long,
) extends DiscordEntity {

  override def getId: Long = id

  override def getApi: DiscordApi =
    throw new RuntimeException("MockDiscordEntity.getApi")

}
