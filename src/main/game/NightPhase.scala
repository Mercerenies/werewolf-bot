
package com.mercerenies.werewolf
package game

import id.{Id, UserMapping}
import board.{Board, Position}
import response.FeedbackMessage
import context.{GameContext, ContextResult}
import record.RecordedGameHistory
import role.instance.RoleInstance

import org.javacord.api.entity.user.User

sealed trait NightPhase {

  def getAction(roleInstance: RoleInstance, userId: Id[User]): GameContext[Unit]

}

object NightPhase {

  case object Dusk extends NightPhase {
    override def getAction(roleInstance: RoleInstance, userId: Id[User]): GameContext[Unit] =
      roleInstance.duskAction(userId)
  }

  case object Night extends NightPhase {
    override def getAction(roleInstance: RoleInstance, userId: Id[User]): GameContext[Unit] =
      roleInstance.nightAction(userId)
  }

}
