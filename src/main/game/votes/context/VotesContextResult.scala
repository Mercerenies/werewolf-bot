
package com.mercerenies.werewolf
package game
package votes
package context

import id.Id
import record.RecordedGameHistory

import org.javacord.api.entity.user.User

case class VotesContextResult[+A](
  val deaths: DeathRoster[Id[User]],
  val history: RecordedGameHistory,
  val result: A,
)
