
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import instance.{RoleInstance, WerewolfRoleInstance}
import util.TextDecorator.*
import util.Grammar
import wincon.{WinCondition, MinionWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.Board
import response.FeedbackMessage
import context.GameContext
import record.ActionPerformedRecord
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Minion extends Role {

  override class Instance(private val mapping: UserMapping) extends RoleInstance {

    override val role: Minion.type = Minion.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] = {
      import ActionPerformedRecord.*
      for {
        board <- GameContext.getBoard
        message <- {
          val werewolfIds = WerewolfRoleInstance.findWerewolfIds(board)
          if (werewolfIds.isEmpty) {
            // No werewolves, so show a special message.
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("was informed that there is no werewolf team.")
              })
            } yield {
              FeedbackMessage("There are " + bold("no werewolves") + " at this time.")
            }
          } else {
            WerewolfRoleInstance.shareWerewolfTeam(mapping, this, userId, werewolfIds)
          }
        }
      } yield {
        message
      }
    }

    override val winCondition: WinCondition =
      MinionWinCondition

  }

  override val name: String = "Minion"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Werewolf

  override val baseWinCondition: WinCondition = MinionWinCondition

  override val precedence: Int = Precedence.MINION

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Minion.Instance(mapping)

  override val introBlurb: String =
    "You are the " + bold("Minion") + ". You will be informed of who the werewolves are, and you win if they win. You are not a werewolf. If there are no werewolves, you may win with the town."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Werewolf)

}
