
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
import instance.{RoleInstance, WerewolfRoleInstance}
import util.Grammar
import util.TextDecorator.*
import wincon.{WinCondition, WerewolfWinCondition}
import night.{NightMessageHandler, ChoiceMessageHandler}
import board.{Board, TablePosition}
import response.FeedbackMessage
import context.GameContext
import choice.syntax.*
import record.ActionPerformedRecord

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Werewolf extends Role {

  override class Instance(private val mapping: UserMapping) extends WerewolfRoleInstance(mapping) {

    override val role: Werewolf.type = Werewolf.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

  }

  override val name: String = "Werewolf"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Werewolf

  override val baseWinCondition: WinCondition = WerewolfWinCondition

  override val precedence: Int = Precedence.WEREWOLF

  override def baseSeenAs: List[GroupedRoleIdentity] =
    List(GroupedRoleIdentity.Werewolf)

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Werewolf.Instance(mapping)

  override val introBlurb: String =
    "You are a " + bold("Werewolf") + ". You will be informed of who the other werewolves are. If there are no other werewolves, you may look at a card in the center of the table."

}
