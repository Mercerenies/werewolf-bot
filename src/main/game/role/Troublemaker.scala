
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
import night.{NightMessageHandler, ChoiceMessageHandler}
import board.{Board, Position, TablePosition}
import response.FeedbackMessage
import choice.syntax.*
import parser.assignment.NamedUser
import context.GameContext
import record.ActionPerformedRecord

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Troublemaker extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends RoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Troublemaker.type = Troublemaker.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        (noValue :+: choiceFactory.twoPlayersNotSelfOption(initialUserId)).formattedList
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right((a, b))) => Some((a, b))
      }

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] = {
      import ActionPerformedRecord.*
      val playerChoice = nightHandlerImpl.currentChoice
      for {
        board <- GameContext.getBoard
        message <- playerChoice match {
          case None => {
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("chose not to swap any cards.")
              })
            } yield {
              FeedbackMessage(s"You elected to swap nobody's cards.")
            }
          }
          case Some((a, b)) => {
            val newBoard = board.swap(Position.Player(a.id), Position.Player(b.id))
            for {
              _ <- GameContext.setBoard(newBoard)
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("swapped the cards in front of ")
                playerName(a.id)
                t(" and ")
                playerName(b.id)
              })
              _ <- GameContext.recordCurrentBoard
            } yield {
              FeedbackMessage(s"You have chosen to swap the cards of ${bold(a.name)} and ${bold(b.name)}.")
            }
          }
        }
      } yield {
        message
      }
    }

    override val winCondition: WinCondition =
      TownWinCondition

  }

  override val name: String = "Troublemaker"

  override val aliases: List[String] = List("TM", "Trouble")

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.TROUBLEMAKER

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Troublemaker.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Troublemaker") + ". You may swap two other players' cards."

}
