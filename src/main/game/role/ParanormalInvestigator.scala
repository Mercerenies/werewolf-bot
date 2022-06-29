
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
import util.{Grammar, Cell}
import util.TextDecorator.*
import wincon.{WinCondition, TownWinCondition}
import night.{NightMessageHandler, ChoiceMessageHandler}
import board.{Board, Position, TablePosition}
import board.snapshot.{RoleSnapshot, SimpleRoleSnapshot}
import response.FeedbackMessage
import choice.formatter.ChoiceFormatter
import choice.syntax.*
import parser.assignment.NamedUser
import context.GameContext
import record.ActionPerformedRecord

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object ParanormalInvestigator extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends CopyingRoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: ParanormalInvestigator.type = ParanormalInvestigator.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        (noValue :+: choiceFactory.oneOrTwoPlayersNotSelfOption(initialUserId)).formattedList
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right((first, optSecond))) => Some((first, optSecond))
      }(using customChoiceFormatter)

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    // Returns feedback and also whether or not to continue
    private def lookAt(userId: Id[User], target: Id[User]): GameContext[(FeedbackMessage, Boolean)] = {
      import ActionPerformedRecord.*
      for {
        board <- GameContext.getBoard
        viewedRole = board(target).role
        shouldCopyWincon = (viewedRole.baseAlignment != Alignment.Town)
        _ <- if (shouldCopyWincon) {
          for {
            _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
              t("looked at the card in front of ")
              playerName(target)
              t(", saw ")
              roleName(viewedRole)
              t(", and copied their win condition and alignment.")
            })
            _ <- GameContext.perform {
              this.copiedRole = Some(viewedRole.createInstance(mapping, initialUserId))
            }
            _ <- GameContext.recordCurrentBoard
          } yield {
            ()
          }
        } else {
          GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
            t("looked at the card in front of ")
            playerName(target)
            t(" and saw ")
            roleName(viewedRole)
            t(".")
          })
        }
      } yield {
        val baseMessage = FeedbackMessage(s"The card in front of ${bold(mapping.nameOf(target))} is ${bold(viewedRole.name)}.")
        val message = if (shouldCopyWincon) {
          baseMessage ++ FeedbackMessage(bold("You have copied their alignment and win condition."), viewedRole.baseWinCondition.blurb)
        } else {
          baseMessage
        }
        (message, !shouldCopyWincon)
      }
    }

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] = {
      import ActionPerformedRecord.*
      val playerChoice = nightHandlerImpl.currentChoice
      for {
        board <- GameContext.getBoard
        message <- playerChoice match {
          case None => {
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("chose not to look at any cards.")
              })
            } yield {
              FeedbackMessage(s"You elected not to look at any cards.")
            }
          }
          case Some((first, optSecond)) => {
            lookAt(userId, first.id) >>= {
              case (firstMessage, true) if optSecond.isDefined => {
                lookAt(userId, optSecond.get.id) map { (secondMessage, _) => firstMessage ++ secondMessage }
              }
              case (firstMessage, _) => {
                // In this case, either we saw a non-town card or we
                // chose not to look at a second card. In either case,
                // don't continue.
                firstMessage.point
              }
            }
          }
        }
      } yield {
        message
      }
    }

    override def defaultWinCondition: WinCondition =
      TownWinCondition

    override def toSnapshot: RoleSnapshot =
      copiedRole match {
        case None => SimpleRoleSnapshot(this.role)
        case Some(instance) => CopiedRoleSnapshot(instance.winCondition)
      }

  }

  // Role snapshot for a PI who has copied a wincon from someone else.
  private class CopiedRoleSnapshot(private val winCondition: WinCondition) extends RoleSnapshot {

    override def name: String = s"Paranormal Investigator [${winCondition.snapshotSummary}]"

  }

  private val customChoiceFormatter: ChoiceFormatter[Either[NoValue, (NamedUser, Option[NamedUser])]] = {
    given ChoiceFormatter[(NamedUser, Option[NamedUser])] = OptionalSecondChoiceFormatter
    summon
  }

  private object OptionalSecondChoiceFormatter extends ChoiceFormatter[(NamedUser, Option[NamedUser])] {

    override def format(value: (NamedUser, Option[NamedUser])): String = {
      val (first, optSecond) = value
      optSecond match {
        case None => ChoiceFormatter.format(first)
        case Some(second) => ChoiceFormatter.format((first, second))
      }
    }

  }

  override val name: String = "Paranormal Investigator"

  override val aliases: List[String] = List("paranormal", "paranormalinvestigator")

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.PARANORMAL_INVESTIGATOR

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    ParanormalInvestigator.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Paranormal Investigator") + ". You may look at up to two other players' cards. If you look at a non-town card, you must stop looking, and your win condition becomes that of the card you looked at."

}
