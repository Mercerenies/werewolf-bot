
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import instance.RoleInstance
import util.TextDecorator.*
import wincon.{WinCondition, TownWinCondition}
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

object Hunter extends Role {

  override class Instance extends RoleInstance {

    override val role: Hunter.type = Hunter.this

    override val precedence: Int = Precedence.NO_ACTION

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[Unit] =
      ().point

    override def votePhaseAction(userId: Id[User]): VotesContext[Boolean] =
      import ActionPerformedRecord.*
      for {
        votals <- VotesContext.getVotals
        roster <- VotesContext.getDeathRoster
        hunterIsDying = roster.isDead(userId)
        hunterTarget = votals(userId)
        modified <- if (hunterIsDying) {
          VotesContext.killPlayer(hunterTarget)
        } else {
          false.point[VotesContext]
        }
        _ <- whenM(modified) {
          VotesContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
            t("is going to be killed and takes ")
            playerName(hunterTarget)
            t(" with them")
          })
        }
      } yield {
        modified
      }

    override val winCondition: WinCondition =
      TownWinCondition

    override val votesPrecedence: Int = VotesPrecedence.HUNTER

  }

  override val name: String = "Hunter"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Hunter.Instance()

  override val introBlurb: String =
    "You are the " + bold("Hunter") + ". If you die, then whoever you vote for dies as well."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Werewolf)

}
