
package com.mercerenies.werewolf
package game
package context

import board.{Board, Position, PlayerOrder}
import id.Id

import org.javacord.api.entity.user.User

// The current state of a GameContext.
case class ContextState(
  val board: Board,
  val playerOrder: PlayerOrder,
  val revealedCards: Set[Position] = Set(),
) {

  def withRevealed(pos: Position): ContextState =
    copy(revealedCards = revealedCards + pos)

}
