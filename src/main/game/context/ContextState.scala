
package com.mercerenies.werewolf
package game
package context

import board.Board
import id.Id

import org.javacord.api.entity.user.User

// The current state of a GameContext.
case class ContextState(
  val board: Board,
  val userIds: List[Id[User]],
)
