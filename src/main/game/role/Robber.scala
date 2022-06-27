
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
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

object Robber extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends RoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Robber.type = Robber.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        (noValue :+: choiceFactory.playerNotSelfOption(initialUserId)).formattedList
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right(a)) => Some(a)
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
                t("chose not to rob anyone")
              })
            } yield {
              FeedbackMessage(s"You elected to rob nobody's cards.")
            }
          }
          case Some(a) => {
            val stolenCard = board(a.id).role
            val newBoard = board.swap(Position.Player(userId), Position.Player(a.id))
            for {
              _ <- GameContext.setBoard(newBoard)
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("swapped their own card with that of ")
                playerName(a.id)
                t(" and saw that their new card was ")
                roleName(stolenCard)
                t(".")
              })
              _ <- GameContext.recordCurrentBoard
            } yield {
              FeedbackMessage(s"You have stolen the ${bold(stolenCard.name)} from ${bold(a.name)}.")
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

  override val name: String = "Robber"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.ROBBER

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Robber.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Robber") + ". You may select a player and swap your card with their own, looking at your new card in the process."

}
