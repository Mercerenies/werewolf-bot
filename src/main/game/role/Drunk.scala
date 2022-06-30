
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

object Drunk extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends RoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Drunk.type = Drunk.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        choiceFactory.tablePosition
      ) {
        x => x
      }

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): GameContext[Unit] = {
      import ActionPerformedRecord.*
      val (playerChoice, forgotMessage) = nightHandlerImpl.currentChoice match {
        case None => (TablePosition.Left, FeedbackMessage("Assuming you copied the " + bold("left card") + "."))
        case Some(x) => (x, FeedbackMessage.none)
      }
      for {
        board <- GameContext.getBoard
        mainMessage <- {
          val newBoard = board.swap(Position.Player(userId), Position.Table(playerChoice))
          for {
            _ <- GameContext.setBoard(newBoard)
            _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
              t("swapped their own card with the ")
              position(playerChoice)
              t(" card.")
            })
            _ <- GameContext.recordCurrentBoard
          } yield {
            FeedbackMessage(s"You have swapped your card with the ${bold(playerChoice.name)} card.")
          }
        }
        _ <- GameContext.feedback(userId, forgotMessage ++ mainMessage)
      } yield {
        ()
      }
    }

    override val winCondition: WinCondition =
      TownWinCondition

  }

  override val name: String = "Drunk"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.DRUNK

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Drunk.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Drunk") + ". You must select a center card. Your card will be swapped with that center card. You do not get to see either card in the process."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Werewolf)

}
