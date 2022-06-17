
package com.mercerenies.werewolf
package game

import id.{Id, UserMapping}
import board.Board
import response.FeedbackMessage
import context.GameContext
import record.RecordedGameHistory

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object NightPhaseEvaluator {

  def evaluate(board: Board, ids: List[Id[User]], records: RecordedGameHistory): NightPhaseResult = {
    val instances = board.playerRoleInstances.sortBy { (_, roleInstance) => - roleInstance.role.precedence }
    val computation: GameContext[List[(Id[User], FeedbackMessage)]] = instances.traverse { (userId, roleInstance) =>
      roleInstance.nightAction(userId).map { (userId, _) }
    }
    val (finalBoard, finalRecords, feedback) = computation.run(board, ids, records)
    NightPhaseResult(finalBoard, finalRecords, feedback.toMap)
  }

}
