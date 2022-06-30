
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
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Exposer extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends RoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Exposer.type = Exposer.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        (noValue :+: choiceFactory.tablePosition).formattedList
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right(pos)) => Some(pos)
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
                t("chose not to expose any cards.")
              })
            } yield {
              FeedbackMessage(s"You elected to expose no cards.")
            }
          }
          case Some(pos) => {
            val centerCard = board(pos).role
            for {
              _ <- GameContext.revealCard(Position.Table(pos))
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("flipped over the ")
                position(pos)
                t(" card, which was ")
                roleName(centerCard)
                t(".")
              })
            } yield {
              FeedbackMessage(s"You flipped over the ${bold(pos.name)} card, revealing that it was ${bold(centerCard.name)}.")
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

  override val name: String = "Exposer"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.EXPOSER

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Exposer.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Exposer") + ". You may choose to flip over a center card, publicly revealing the card at day start."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Alien)

}
