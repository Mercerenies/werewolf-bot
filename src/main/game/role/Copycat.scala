
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
import board.Board
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

    override val precedence: Int = Precedence.COPYCAT

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val duskHandlerImpl: CopycatMessageHandler =
      CopycatMessageHandler(mapping, initialUserId, this)

    override def duskHandler: NightMessageHandler =
      duskHandlerImpl

    override def nightAction(userId: Id[User]): GameContext[Unit] =
      this.copiedRole match {
        case None => {
          // Should not happen; a Copycat who was in play at dusk
          // should have copied someone, and one put into play by
          // Witch or Drunk or something shouldn't have been swapped
          // by this point.
          logger.error(s"Copycat belonging to ${mapping.nameOf(userId)} (${userId}) has copied nothing in nightAction")
          ().point
        }
        case Some(instance) => {
          instance.nightAction(userId)
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
