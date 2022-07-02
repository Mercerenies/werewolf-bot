
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import instance.{RoleInstance, WerewolfRoleInstance}
import id.{Id, UserMapping}
import util.Grammar
import util.TextDecorator.*
import wincon.{WinCondition, FluffyRipperWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.{Board, TablePosition}
import response.FeedbackMessage
import context.GameContext
import choice.syntax.*
import record.ActionPerformedRecord
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

final class FluffyRipper private(
  override val name: String,
  val identity: GroupedRoleIdentity,
  val target: GroupedRoleIdentity,
) extends Role {

  override class Instance(private val mapping: UserMapping) extends WerewolfRoleInstance(mapping) {
    import Instance.logger

    override val role: FluffyRipper.this.type = FluffyRipper.this

    override val precedence: Int = Precedence.WEREWOLF

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override def winCondition: WinCondition =
      FluffyRipperWinCondition(owner = FluffyRipper.this, target = target)

    override def seenAs: List[GroupedRoleIdentity] =
      identity :: super.seenAs

  }

  object Instance extends Logging[Instance]

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Werewolf

  override val baseWinCondition: WinCondition =
    FluffyRipperWinCondition(owner = this, target = target)

  override def baseSeenAs: List[GroupedRoleIdentity] =
    List(GroupedRoleIdentity.Werewolf, identity)

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    this.Instance(mapping)

  override val introBlurb: String =
    "You are " + bold(name) + ". You count as a werewolf and will be informed of the other werewolves. Like a werewolf, you may choose a center card to view if there are no other werewolves."

  override def inspiration: Inspiration =
    Inspiration.Custom

}

object FluffyRipper {

  val Fluffy =
    FluffyRipper(name = "Fluffy", identity = GroupedRoleIdentity.Fluffy, target = GroupedRoleIdentity.Ripper)
  val Ripper =
    FluffyRipper(name = "Ripper", identity = GroupedRoleIdentity.Ripper, target = GroupedRoleIdentity.Fluffy)

}
