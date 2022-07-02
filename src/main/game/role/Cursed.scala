
package com.mercerenies.werewolf
package game
package role

import util.Cell
import id.{Id, UserMapping}
import instance.{RoleInstance, WerewolfRoleInstance}
import util.TextDecorator.*
import wincon.{WinCondition, TownWinCondition, WerewolfWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.Board
import response.FeedbackMessage
import context.GameContext
import votes.context.VotesContext
import record.ActionPerformedRecord
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Cursed extends Role {

  override class Instance extends RoleInstance {

    override val role: Cursed.type = Cursed.this

    override val precedence: Int = Precedence.NO_ACTION

    private val isTurnedCell: Cell[Boolean] =
      Cell(false)

    def isTurned: Boolean =
      isTurnedCell.value

    def turnToWerewolf(): Unit = {
      isTurnedCell.value = true
    }

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[Unit] =
      ().point

    override def votePhaseAction(userId: Id[User]): VotesContext[Boolean] =
      import ActionPerformedRecord.*
      for {
        board <- VotesContext.getBoard
        votals <- VotesContext.getVotals
        votesForCursed = votals.reverseMapping.getOrElse(userId, Nil).toSet
        werewolves = WerewolfRoleInstance.findWerewolfIds(board).toSet
        werewolfVotesForCursed = votesForCursed & werewolves
        modified = ((!isTurned) && (werewolfVotesForCursed.nonEmpty))
        _ <- whenM(modified) {
          VotesContext.perform { this.turnToWerewolf() }
        }
      } yield {
        modified
      }

    override def winCondition: WinCondition =
      if (isTurned) {
        WerewolfWinCondition
      } else {
        TownWinCondition
      }

    override def seenAs: List[GroupedRoleIdentity] =
      if (isTurned) {
        List(GroupedRoleIdentity.Werewolf)
      } else {
        List()
      }

    override val votesPrecedence: Int = VotesPrecedence.CURSED

  }

  override val name: String = "Cursed"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Cursed.Instance()

  override val introBlurb: String =
    "You are the " + bold("Cursed") + ". If any werewolves vote for you, then you become a werewolf."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Bonus)

}
