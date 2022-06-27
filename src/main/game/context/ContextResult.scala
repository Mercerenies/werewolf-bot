
package com.mercerenies.werewolf
package game
package context

import board.{Board, Position}
import id.Id
import record.RecordedGameHistory

import org.javacord.api.entity.user.User

// The result of GameContext.run
case class ContextResult[+A](
  // The game board state.
  val board: Board,
  // All game history events.
  val history: RecordedGameHistory,
  // The set of revealed cards.
  val revealedCards: Set[Position],
  // The monadic result.
  val result: A,
)
