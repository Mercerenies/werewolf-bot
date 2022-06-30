
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
import instance.RoleInstance
import util.Grammar
import util.TextDecorator.*
import wincon.{WinCondition, TownWinCondition}
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

object Mason extends Role {

  override class Instance(private val mapping: UserMapping) extends RoleInstance {
    import Instance.logger

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Mason.type = Mason.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[Unit] = {
      import ActionPerformedRecord.*
      for {
        board <- GameContext.getBoard
        _ <- {
          val masonIds = findMasonIds(board)
          if (masonIds.length <= 1) {
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("was informed that they are the ")
                b { t("only mason") }
              })
              _ <- GameContext.feedback(userId, "You are the " + bold("only mason") + ".")
            } yield {
              ()
            }
          } else {
            val masonNames = masonIds.map { mapping.nameOf(_) }.sorted
            val masonNamesList = Grammar.conjunctionList(masonNames)
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("was informed that the werewolf team consists of ")
                b { t(masonNamesList) }
              })
              _ <- GameContext.feedback(userId, "The mason team consists of " + bold(masonNamesList) + ".")
            } yield {
              ()
            }
          }
        }
      } yield {
        ()
      }
    }

    override val winCondition: WinCondition =
      TownWinCondition

    override def seenAs: List[GroupedRoleIdentity] =
      List(GroupedRoleIdentity.Mason)

  }

  object Instance extends Logging[Instance]

  override val name: String = "Mason"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.MASON

  override def baseSeenAs: List[GroupedRoleIdentity] =
    List(GroupedRoleIdentity.Mason)

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Mason.Instance(mapping)

  override val introBlurb: String =
    "You are a " + bold("Mason") + ". You will be informed of who the other masons are."

  def findMasonIds(board: Board): List[Id[User]] =
    board.playerRoleInstances.filter { (_, instance) =>
      instance.seenAs.contains(GroupedRoleIdentity.Mason)
    }.map { (userId, _) =>
      userId
    }

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Werewolf)
}
