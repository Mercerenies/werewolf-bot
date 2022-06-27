
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
import util.Grammar
import util.TextDecorator.*
import wincon.{WinCondition, WerewolfWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.{Board, TablePosition}
import response.FeedbackMessage
import context.GameContext
import choice.syntax.*
import record.ActionPerformedRecord

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object DreamWolf extends Role {

  override class Instance(private val mapping: UserMapping) extends RoleInstance {
    import Instance.logger

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: DreamWolf.type = DreamWolf.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] =
      FeedbackMessage.none.point

    override val winCondition: WinCondition =
      WerewolfWinCondition

    override def seenAs: List[GroupedRoleIdentity] =
      List(GroupedRoleIdentity.Werewolf)

  }

  object Instance extends Logging[Instance]

  override val name: String = "Dream Wolf"

  override val aliases: List[String] = List("DW", "Dreamwolf")

  override val baseAlignment: Alignment = Alignment.Werewolf

  override val precedence: Int = Precedence.NO_ACTION

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    DreamWolf.Instance(mapping)

  override val introBlurb: String =
    "You are a " + bold("Dream Wolf") + ". You are a member of the werewolf team but you do not know who the other werewolves (if any) are. They are aware of you."

}