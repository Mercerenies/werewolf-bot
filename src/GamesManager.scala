
package com.mercerenies.werewolf

import org.javacord.api.entity.channel.TextChannel

import scala.collection.mutable.HashMap

final class GamesManager {

  // Mapping from channel ID to game data
  private val games: HashMap[Long, Unit] = HashMap()

  def createGame(channelId: Long): Unit = {
    games(channelId) = ()
    println("Created game...")
  }

  def createGame(channel: TextChannel): Unit = {
    createGame(channel.getId())
  }

}
