
package com.mercerenies.werewolf
package game
package context

import board.{Board, Position, PlayerOrder}
import response.FeedbackMessage
import id.Id

import org.javacord.api.entity.user.User

// The current state of a GameContext.
case class ContextState(
  val board: Board,
  val playerOrder: PlayerOrder,
  val playerFeedback: Map[Id[User], FeedbackMessage] = Map(),
  val revealedCards: Set[Position] = Set(),
) {

  def feedback(user: Id[User], message: FeedbackMessage): ContextState = {
    val currentFeedback = playerFeedback.getOrElse(user, FeedbackMessage.none)
    copy(playerFeedback = playerFeedback + ((user, currentFeedback ++ message)))
  }

  def withRevealed(pos: Position): ContextState =
    copy(revealedCards = revealedCards + pos)

}
