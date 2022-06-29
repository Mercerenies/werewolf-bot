
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
import instance.RoleInstance
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

  override class Instance(private val mapping: UserMapping) extends RoleInstance {
    import Instance.logger

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Werewolf.type = Werewolf.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        (noValue :+: choiceFactory.tablePosition).formattedList
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right(x)) => Some(x)
      }

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] = {
      import ActionPerformedRecord.*
      val tablePos = nightHandlerImpl.currentChoice
      for {
        board <- GameContext.getBoard
        message <- {
          val werewolfIds = findWerewolfIds(board)
          if (werewolfIds.length <= 1) {
            // There's one werewolf, so look at the center card.
            viewCenterCard(this, userId, tablePos)
          } else {
            shareWerewolfTeam(mapping, this, userId, werewolfIds)
          }
        }
      } yield {
        message
      }
    }

    override val winCondition: WinCondition =
      WerewolfWinCondition

    override def seenAs: List[GroupedRoleIdentity] =
      List(GroupedRoleIdentity.Werewolf)

  }

  object Instance extends Logging[Instance]

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

  def findWerewolfIds(board: Board): List[Id[User]] =
    board.playerRoleInstances.filter { (_, instance) =>
      instance.seenAs.contains(GroupedRoleIdentity.Werewolf)
    }.map { (userId, _) =>
      userId
    }

  def shareWerewolfTeam(mapping: UserMapping, instance: RoleInstance, userId: Id[User], werewolfIds: Iterable[Id[User]]): GameContext[FeedbackMessage] = {
    import ActionPerformedRecord.*
    val names = werewolfIds.toList.map { mapping.nameOf(_) }.sorted
    val namesList = Grammar.conjunctionList(names)
    for {
      _ <- GameContext.record(ActionPerformedRecord(instance.toSnapshot, userId) {
        t("was informed that the werewolf team consists of ")
        b { t(namesList) }
        t(".")
      })
    } yield {
      FeedbackMessage("The werewolf team consists of " + bold(namesList) + ".")
    }
  }

  def viewCenterCard(instance: RoleInstance, userId: Id[User], tablePos: Option[TablePosition]): GameContext[FeedbackMessage] = {
    import ActionPerformedRecord.*
    tablePos match {
      case None => {
        for {
          _ <- GameContext.record(ActionPerformedRecord(instance.toSnapshot, userId) {
            t("is the ")
            b { t("solo werewolf") }
            t(" and chose not to look at any cards")
          })
        } yield {
          FeedbackMessage("You are the " + bold("solo werewolf") + ". You elected not to look at any cards.")
        }
      }
      case Some(tablePos) => {
        for {
          board <- GameContext.getBoard
          centerCard = board(tablePos).role
          _ <- GameContext.record(ActionPerformedRecord(instance.toSnapshot, userId) {
            t("is the ")
            b { t("solo werewolf") }
            t(" and saw that the ")
            position(tablePos)
            t(" card was ")
            roleName(centerCard)
          })
        } yield {
          FeedbackMessage("You are the " + bold("solo werewolf") + ". The " + bold(tablePos.toString) + " card is " + bold(centerCard.name) + ".")
        }
      }
    }
  }

}
