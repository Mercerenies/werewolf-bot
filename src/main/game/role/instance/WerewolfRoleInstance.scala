
package com.mercerenies.werewolf
package game
package role
package instance

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
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

// A trait for roles that behave like werewolves. This trait provides
// functionality for looking at a center card if alone and for getting
// the werewolf team. Basically, this trait is suitable for use
// out-of-the-box as a default werewolf and can be used for other
// werewolf variants when convenient.
//
// This trait should *not* be used as a typecheck to determine if a
// role is a werewolf role. Use of this trait is optional and is
// provided as a convenience. Whether or not a role is a werewolf
// should be checked against RoleInstance.seenAs (to check whether the
// instance is grouped with werewolves) or Role.baseAlignment (to
// check whether the card itself is associated with the werewolf
// team).
trait WerewolfRoleInstance(private val mapping: UserMapping) extends RoleInstance {

  private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

  private val nightHandlerImpl =
    ChoiceMessageHandler(
      (noValue :+: choiceFactory.tablePosition).formattedList
    ) {
      case None | Some(Left(NoValue)) => None
      case Some(Right(x)) => Some(x)
    }

  // Note: If an implementing class overrides this, they must override
  // nightAction as well, as WerewolfRoleInstance.nightAction only
  // makes sense with the default handler.
  override def nightHandler: NightMessageHandler =
    nightHandlerImpl

  override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] = {
    import ActionPerformedRecord.*
    import WerewolfRoleInstance.{shareWerewolfTeam, viewCenterCard, findWerewolfIds}
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

  override def winCondition: WinCondition =
    WerewolfWinCondition

  override def seenAs: List[GroupedRoleIdentity] =
    List(GroupedRoleIdentity.Werewolf)

}

object WerewolfRoleInstance {

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

  def findWerewolfIds(board: Board): List[Id[User]] =
    board.playerRoleInstances.filter { (_, instance) =>
      instance.seenAs.contains(GroupedRoleIdentity.Werewolf)
    }.map { (userId, _) =>
      userId
    }

}