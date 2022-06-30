
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

object Thing extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends RoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Thing.type = Thing.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        (noValue :+: choiceFactory.direction).formattedList
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right(dir)) => Some(dir)
      }

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): GameContext[Unit] = {
      import ActionPerformedRecord.*
      val playerChoice = nightHandlerImpl.currentChoice
      for {
        board <- GameContext.getBoard
        playerOrder <- GameContext.getPlayerOrder
        _ <- playerChoice match {
          case None => {
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("chose not to bump anyone.")
              })
              _ <- GameContext.feedback(userId, "You chose not to bump anyone.")
            } yield {
              ()
            }
          }
          case Some(dir) => {
            val adjacentPlayer = playerOrder.sideOf(userId, dir)
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("bumped ")
                playerName(adjacentPlayer)
                t(", the player to their immediate ")
                direction(dir)
                t(".")
              })
              _ <- GameContext.feedback(userId, s"You bumped the player to your ${bold(dir.name)}, ${bold(mapping.nameOf(adjacentPlayer))}.")
              boldThing = bold("Thing")
              _ <- GameContext.feedback(adjacentPlayer, s"A ${boldThing} went bump in the night, on your ${bold(dir.other.name)} side.")
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

  }

  override val name: String = "Thing"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.THING

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Thing.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Thing") + " (that goes bump in the night). You may choose to bump either the player to your left or the player to your right, informing them that the Thing is next to them."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Bonus)

}
