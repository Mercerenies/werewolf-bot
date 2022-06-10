
package com.mercerenies.werewolf
package game
package role

import id.Id
import name.NamedEntity
import night.{NightMessageHandler, NoInputNightMessageHandler}
import response.FeedbackMessage
import board.Board

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

final class StatelessRoleInstance(
  override val role: Role,
)(
  using isValid: (StatelessRoleInstance <:< role.Instance),
) extends RoleInstance {

  val coherenceProof: this.type <:< this.role.Instance =
    summon[this.type <:< StatelessRoleInstance] andThen isValid

  val nightHandler: NightMessageHandler =
    NoInputNightMessageHandler

  def nightAction(userId: Id[User]): State[Board, FeedbackMessage] =
    FeedbackMessage.none.point

}
