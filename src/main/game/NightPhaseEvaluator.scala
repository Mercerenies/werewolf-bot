
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

  def evaluate(board: Board, order: PlayerOrder, records: RecordedGameHistory): NightPhaseResult = {
    val instances = board.playerRoleInstances.sortBy { (_, roleInstance) => - roleInstance.role.precedence }
    val computation: GameContext[List[(Id[User], FeedbackMessage)]] = instances.traverse { (userId, roleInstance) =>
      roleInstance.nightAction(userId).map { (userId, _) }
    }
    val contextResult = computation.run(board, order, records)
    NightPhaseResult.fromContextResult(contextResult)
  }

}
