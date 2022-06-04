
package com.mercerenies.werewolf
package id

import org.javacord.api.entity.DiscordEntity
import org.javacord.api.entity.channel.TextChannel

// Opaque types representing the IDs of different things in Discord.
// These are all represented by Long under the hood, but using these
// types ensures that the correct object is being represented.
opaque type Id[+A <: DiscordEntity] = Long

object Id {

  def fromLong[A <: DiscordEntity](x: Long): Id[DiscordEntity] = x

  def apply[A <: DiscordEntity](x: A): Id[A] = x.getId()

  extension[A <: DiscordEntity](self: Id[A])
    def toLong: Long = self

}
