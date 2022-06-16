
package com.mercerenies.werewolf
package game
package context

import board.Board
import record.RecordedGameHistory

// The current state of a GameContext.
case class ContextState(
  val history: RecordedGameHistory,
  val board: Board,
)
