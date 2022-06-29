
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
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

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

final class FluffyRipper private(
  override val name: String,
  val identity: GroupedRoleIdentity,
  val target: GroupedRoleIdentity,
) extends Role {

  override class Instance(private val mapping: UserMapping) extends RoleInstance {
    import Instance.logger

    override val role: FluffyRipper.this.type = FluffyRipper.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] = {
      import ActionPerformedRecord.*
      for {
        board <- GameContext.getBoard
        message <- {
          val werewolfIds = Werewolf.findWerewolfIds(board)
          val werewolfNames = werewolfIds.map { mapping.nameOf(_) }.sorted
          val werewolfNamesList = Grammar.conjunctionList(werewolfNames)
          for {
            _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
              t("was informed that the werewolf team consists of ")
              b { t(werewolfNamesList) }
              t(".")
            })
          } yield {
            FeedbackMessage("The werewolf team consists of " + bold(werewolfNamesList) + ".")
          }
        }
      } yield {
        message
      }
    }

    override val winCondition: WinCondition =
      FluffyRipperWinCondition(owner = FluffyRipper.this, target = target)

    override def seenAs: List[GroupedRoleIdentity] =
      List(GroupedRoleIdentity.Werewolf, identity)

  }

  object Instance extends Logging[Instance]

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Werewolf

  override val baseWinCondition: WinCondition =
    FluffyRipperWinCondition(owner = this, target = target)

  override val precedence: Int = Precedence.WEREWOLF

  override def baseSeenAs: List[GroupedRoleIdentity] =
    List(GroupedRoleIdentity.Werewolf, identity)

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    this.Instance(mapping)

  override val introBlurb: String =
    "You are " + bold(name) + ". You count as a werewolf and will be informed of the other werewolves."

}

object FluffyRipper {

  val Fluffy =
    FluffyRipper(name = "Fluffy", identity = GroupedRoleIdentity.Fluffy, target = GroupedRoleIdentity.Ripper)
  val Ripper =
    FluffyRipper(name = "Ripper", identity = GroupedRoleIdentity.Ripper, target = GroupedRoleIdentity.Fluffy)

}
