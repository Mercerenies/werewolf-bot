
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import id.{Id, UserMapping}
import instance.{RoleInstance, CopyingRoleInstance}
import util.TextDecorator.*
import util.Grammar
import wincon.{WinCondition, TownWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler, CopycatMessageHandler}
import board.{Board, TablePosition}
import board.snapshot.{RoleSnapshot, SimpleRoleSnapshot}
import response.FeedbackMessage
import context.GameContext
import record.ActionPerformedRecord
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Copycat extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends CopyingRoleInstance {
    import Instance.logger

    override val role: Copycat.type = Copycat.this

    override def duskPrecedence: Int =
      Precedence.COPYCAT

    override def precedence: Int =
      copiedRole.fold(Precedence.COPYCAT) { _.precedence }

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val duskHandlerImpl: CopycatMessageHandler =
      CopycatMessageHandler(mapping, initialUserId, this)

    override def duskHandler: NightMessageHandler =
      duskHandlerImpl

    override def duskAction(userId: Id[User]): GameContext[Unit] = {
      // Record which role we copied, and then run its dusk action (if
      // it happens to be a doppelganger or other dusk role, it will
      // have something to do here)
      import ActionPerformedRecord.*
      for {
        board <- GameContext.getBoard
        _ <- whenM (!duskHandlerImpl.hasChoice) {
          val roleToCopy = board(TablePosition.Left).role
          for {
            responseMessage <- GameContext.perform { duskHandlerImpl.copyRole(TablePosition.Left, roleToCopy) }
            _ <- GameContext.feedback(userId, "Assuming you copied the " + bold("left card") + ".")
            _ <- GameContext.feedback(userId, responseMessage)
          } yield {
            ()
          }
        }
        // Note: At this point, the copied role *should* always be
        // present, so the villager thing is just me being overly
        // defensive.
        copiedRoleInstance = copiedRole.getOrElse(Villager.createInstance(mapping, initialUserId))
        _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
          t("copied the ")
          position(duskHandlerImpl.copiedPosition.getOrElse(TablePosition.Left))
          t(" card and became a ")
          roleName(copiedRoleInstance.role)
        })
        _ <- delegatedDuskAction(userId)
      } yield {
        ()
      }
    }

    private def delegatedDuskAction(userId: Id[User]): GameContext[Unit] = {
      val copiedRoleInstance = copiedRole.getOrElse(Villager.createInstance(mapping, initialUserId))
      if (copiedRoleInstance.isInstanceOf[Copycat.Instance]) {
        // A Copycat has copied a Copycat card in the center of the
        // board. This should only happen if someone starts a game
        // with a deliberately contrived setup in an effort to make
        // this game crash. But we need to *not* delegate in this
        // case, because if we do then we'll hit an infinite loop if
        // the Copycat card happens to be in the left position.
        ().point
      } else {
        censored { copiedRoleInstance.duskAction(userId) }
      }
    }

    override def defaultWinCondition: WinCondition =
      TownWinCondition

  }

  object Instance extends Logging[Instance]

  override val name: String = "Copycat"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Copycat.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Copycat") + ". You must choose a center card. You will look at that card and become a copy of it, performing its night actions and taking on its alignment and win condition."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Vampire)

  override val requiresDuskPhase: Boolean =
    true

}
