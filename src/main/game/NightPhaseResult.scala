
package com.mercerenies.werewolf
package game

import id.{Id, UserMapping}
import board.{Board, Position}
import response.FeedbackMessage
import context.{GameContext, ContextResult}
import record.RecordedGameHistory

import org.javacord.api.entity.user.User

case class NightPhaseResult(
  val board: Board,
  val history: RecordedGameHistory,
  val feedback: Map[Id[User], FeedbackMessage],
  val revealedCards: Set[Position],
)

object NightPhaseResult {

  def fromContextResult[U](result: ContextResult[U]): NightPhaseResult =
    NightPhaseResult(
      board = result.board,
      history = result.history,
      feedback = result.playerFeedback.withDefault(_ => FeedbackMessage.none),
      revealedCards = result.revealedCards,
    )

}
