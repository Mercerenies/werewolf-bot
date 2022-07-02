
package com.mercerenies.werewolf
package game

import id.{Id, UserMapping}
import board.{Board, PlayerOrder}
import response.FeedbackMessage
import context.GameContext
import record.RecordedGameHistory

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object NightPhaseEvaluator {

  def evaluate(board: Board, phase: NightPhase, order: PlayerOrder, records: RecordedGameHistory): NightPhaseResult = {
    val instances = board.playerRoleInstances.sortBy { (_, roleInstance) => - roleInstance.precedence }
    val computation: GameContext[Unit] = instances.traverse { (userId, roleInstance) =>
      phase.getAction(roleInstance, userId)
    }.void
    val contextResult = computation.run(board, order, records)
    NightPhaseResult.fromContextResult(contextResult)
  }

}
