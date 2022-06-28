
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import util.Grammar
import util.TextDecorator.*
import wincon.{WinCondition, TownWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.Board
import response.FeedbackMessage
import context.GameContext
import votes.Votals
import votes.context.VotesContext
import record.ActionPerformedRecord

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Bodyguard extends Role {

  override class Instance(private val mapping: UserMapping) extends RoleInstance {

    override val role: Bodyguard.type = Bodyguard.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] =
      FeedbackMessage.none.point

    override def votePhaseAction(userId: Id[User]): VotesContext[Boolean] =
      import ActionPerformedRecord.*
      for {
        votals <- VotesContext.getVotals
        roster <- VotesContext.getDeathRoster
        bodyguardTarget = votals(userId)
        targetIsDying = roster.isDead(bodyguardTarget)
        modified <- if (targetIsDying) {
          VotesContext.protectPlayer(bodyguardTarget)
        } else {
          false.point[VotesContext]
        }
        _ <- whenM(modified) {
          VotesContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
            t("has protected ")
            playerName(bodyguardTarget)
            t(".")
          })
        }
        _ <- whenM(modified && votals.majority.contains(bodyguardTarget)) {
          val secondMajority = secondMostMajority(votals)
          val secondMajorityNames = Grammar.conjunctionList(secondMajority.map { mapping.nameOf(_) }.sorted)
          VotesContext.killPlayers(secondMajority) >>
            whenM(!secondMajority.isEmpty) {
              VotesContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("has protected the majority, so ")
                b { t(secondMajorityNames) }
                t(" will die instead.")
              })
            }
        }
      } yield {
        modified
      }

    override val winCondition: WinCondition =
      TownWinCondition

    override val votesPrecedence: Int = VotesPrecedence.BODYGUARD

  }

  override val name: String = "Bodyguard"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.NO_ACTION

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Bodyguard.Instance(mapping)

  override val introBlurb: String =
    "You are the " + bold("Bodyguard") + ". Whoever you vote for is incapable of dying. If the player you vote has the majority of votes, then whoever has the second-most dies instead."

  private def secondMostMajority[A](votals: Votals[A]): List[A] = {
    val majority = votals.majority
    val filteredVotals = Votals(votals.toMap.filter { (_, v) => !majority.contains(v) })
    filteredVotals.majority
  }

}
