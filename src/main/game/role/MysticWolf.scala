
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
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object MysticWolf extends Role {

  override class Instance(private val mapping: UserMapping, private val initialUserId: Option[Id[User]]) extends RoleInstance {
    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: MysticWolf.type = MysticWolf.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        (noValue :+: choiceFactory.playerNotSelfOption(initialUserId)).formattedList
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right(x)) => Some(x)
      }

    override def duskAction(userId: Id[User]): GameContext[Unit] = {
      import ActionPerformedRecord.*
      import WerewolfRoleInstance.{shareWerewolfTeam, viewCenterCard, findWerewolfIds}
      for {
        board <- GameContext.getBoard
        _ <- {
          val werewolfIds = findWerewolfIds(board)
          if (werewolfIds.length <= 1) {
            // There's one werewolf. Mystic Wolf does not look in the center.
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("is the ")
                b { t("solo werewolf") }
                t(".")
              })
              _ <- GameContext.feedback(userId, "You are the " + bold("solo werewolf") + ".")
            } yield {
              ()
          }
          } else {
            shareWerewolfTeam(mapping, this, userId)
          }
        }
      } yield {
        ()
      }
    }

    override def nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): GameContext[Unit] = {
      import ActionPerformedRecord.*
      val target = nightHandlerImpl.currentChoice
      target match {
        case None => {
          for {
            _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
              t("chose not to look at any cards")
            })
            _ <- GameContext.feedback(userId, "You elected not to look at any cards.")
          } yield {
            ()
          }
        }
        case Some(player) => {
          for {
            board <- GameContext.getBoard
            card = board(player.id).role
            _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
              t("observed that the card in front of ")
              position(player.id)
              t(" card was ")
              roleName(card)
            })
            _ <- GameContext.feedback(userId, s"The card in front of ${player.displayName} is " + bold(card.name) + ".")
          } yield {
            ()
          }
        }
      }
    }

    override def winCondition: WinCondition =
      WerewolfWinCondition

    override def seenAs: List[GroupedRoleIdentity] =
      List(GroupedRoleIdentity.Werewolf)

  }

  override val name: String = "Mystic Wolf"

  override val aliases: List[String] = List("Mysticwolf")

  override val baseAlignment: Alignment = Alignment.Werewolf

  override val baseWinCondition: WinCondition = WerewolfWinCondition

  override val precedence: Int = Precedence.MYSTIC_WOLF

  override def baseSeenAs: List[GroupedRoleIdentity] =
    List(GroupedRoleIdentity.Werewolf)

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    MysticWolf.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Mystic Wolf") + ". You will be informed of who the other werewolves are. You may choose a player's card and look at it."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Daybreak)

}
