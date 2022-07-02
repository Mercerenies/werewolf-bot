
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

object Insomniac extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends RoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Insomniac.type = Insomniac.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[Unit] = {
      import ActionPerformedRecord.*
      for {
        board <- GameContext.getBoard
        mainMessage <- {
          val insomniacCard = board(userId).role
          for {
            _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
              t("was informed that their card is now ")
              roleName(insomniacCard)
            })
          } yield {
            FeedbackMessage(s"You awake to find that your role card is ${bold(insomniacCard.name)}.")
          }
        }
        _ <- GameContext.feedback(userId, mainMessage)
      } yield {
        ()
      }
    }

    override val winCondition: WinCondition =
      TownWinCondition

  }

  override val name: String = "Insomniac"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.INSOMNIAC

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Insomniac.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Insomniac") + ". At the end of the night, you will be informed of which role you have."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Werewolf)
}
