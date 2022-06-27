
package com.mercerenies.werewolf
package game

import id.{Id, UserMapping}
import board.Board
import response.FeedbackMessage
import context.{GameContext, ContextResult}
import record.RecordedGameHistory

import org.javacord.api.entity.user.User

case class NightPhaseResult(
  val board: Board,
  val history: RecordedGameHistory,
  val feedback: Map[Id[User], FeedbackMessage],
)

object NightPhaseResult {

  def fromContextResult(result: ContextResult[Iterable[(Id[User], FeedbackMessage)]]): NightPhaseResult =
    NightPhaseResult(
      board = result.board,
      history = result.history,
      feedback = result.result.toMap,
    )

}
