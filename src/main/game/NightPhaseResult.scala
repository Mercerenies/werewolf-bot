
package com.mercerenies.werewolf
package game

import id.{Id, UserMapping}
import board.Board
import response.FeedbackMessage
import context.GameContext
import record.RecordedGameHistory

import org.javacord.api.entity.user.User

case class NightPhaseResult(
  val board: Board,
  val history: RecordedGameHistory,
  val feedback: Map[Id[User], FeedbackMessage],
)
