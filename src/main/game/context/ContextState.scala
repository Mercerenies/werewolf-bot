
package com.mercerenies.werewolf
package game
package context

import board.Board

// The current state of a GameContext.
case class ContextState(
  val board: Board,
)
