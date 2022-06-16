
package com.mercerenies.werewolf
package game

import id.{Id, UserMapping}
import board.Board
import response.FeedbackMessage
import context.GameContext

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object NightPhaseEvaluator {

  def evaluate(board: Board): NightPhaseResult = {
    val instances = board.playerRoleInstances.sortBy { (_, roleInstance) => - roleInstance.role.precedence }
    val computation: GameContext[List[(Id[User], FeedbackMessage)]] = instances.traverse { (userId, roleInstance) =>
      roleInstance.nightAction(userId).map { (userId, _) }
    }
    val (finalBoard, records, feedback) = computation.run(board)
    NightPhaseResult(finalBoard, records, feedback.toMap)
  }

}
