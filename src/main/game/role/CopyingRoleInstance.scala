
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import name.NamedEntity
import util.Cell
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.Board
import board.snapshot.{RoleSnapshot, SimpleRoleSnapshot}
import response.FeedbackMessage
import wincon.WinCondition
import context.GameContext
import votes.context.VotesContext

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

// A CopyingRoleInstance is a RoleInstance designed to delegate to
// another. This is used by roles like Doppelganger and Paranormal
// Investigator who can choose to become another role.
trait CopyingRoleInstance extends RoleInstance {

  private val copiedRoleCell: Cell[Option[RoleInstance]] =
    Cell(None)

  def copiedRole: Option[RoleInstance] =
    copiedRoleCell.value

  def copiedRole_=(value: Option[RoleInstance]): Unit = {
    copiedRoleCell.value = value
  }

  override def nightHandler: NightMessageHandler =
    copiedRole.fold(NoInputNightMessageHandler) { _.nightHandler }

  override def votesPrecedence: Int =
    copiedRole.fold(VotesPrecedence.NO_ACTION) { _.votesPrecedence }

  def defaultWinCondition: WinCondition

  override def winCondition: WinCondition =
    copiedRole.fold(defaultWinCondition) { _.winCondition }

  override def seenAs: List[GroupedRoleIdentity] =
    copiedRole.fold(Nil) { _.seenAs }

  override def votePhaseAction(userId: Id[User]): VotesContext[Boolean] =
    copiedRole.fold(false.point[VotesContext]) { _.votePhaseAction(userId) }

  override def toSnapshot: RoleSnapshot

}
