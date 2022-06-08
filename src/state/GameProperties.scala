
package com.mercerenies.werewolf
package state

import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel

case class GameProperties(
  val channel: TextChannel & Nameable,
  val host: User,
)
