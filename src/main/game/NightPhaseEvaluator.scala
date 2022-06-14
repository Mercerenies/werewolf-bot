
package com.mercerenies.werewolf
package game

import id.{Id, UserMapping}
import board.Board
import response.FeedbackMessage

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object NightPhaseEvaluator {

  def evaluate(mapping: UserMapping, board: Board): (Board, List[(Id[User], FeedbackMessage)]) = {
    val instances = board.playerRoleInstances.sortBy { (_, roleInstance) => - roleInstance.role.precedence }
    val stateMonad: State[Board, List[(Id[User], FeedbackMessage)]] = instances.traverse { (userId, roleInstance) =>
      roleInstance.nightAction(mapping, userId).map { (userId, _) }
    }
    // So Scala dies a violent and bloody death if I don't include the
    // 'using' argument here. No idea why, but probably related to
    // https://github.com/lampepfl/dotty/issues/12479.
    stateMonad(board)(using scalaz.Id.id)
  }

}
