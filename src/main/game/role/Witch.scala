
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

object Witch extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends RoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Witch.type = Witch.this

    override val precedence: Int = Precedence.WITCH

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        (noValue :+: (choiceFactory.tablePosition :**: choiceFactory.player)).formattedList
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right((pos, player))) => Some((pos, player))
      }

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): GameContext[Unit] = {
      import ActionPerformedRecord.*
      val playerChoice = nightHandlerImpl.currentChoice
      for {
        board <- GameContext.getBoard
        message <- playerChoice match {
          case None => {
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("chose not to swap anyone")
              })
            } yield {
              FeedbackMessage(s"You elected to take no action.")
            }
          }
          case Some((pos, player)) => {
            val centerCard = board(pos).role
            val newBoard = board.swap(Position.Player(player.id), Position.Table(pos))
            for {
              _ <- GameContext.setBoard(newBoard)
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("saw that the ")
                position(pos)
                t(" card was ")
                roleName(centerCard)
                t(" and swapped it with the card in front of ")
                playerName(player.id)
              })
              _ <- GameContext.recordCurrentBoard
            } yield {
              FeedbackMessage(s"You looked at the ${bold(pos.name)} card, saw that it was ${bold(centerCard.name)}, and swapped it with the card in front of ${bold(player.name)}.")
            }
          }
        }
        _ <- GameContext.feedback(userId, message)
      } yield {
        ()
      }
    }

    override val winCondition: WinCondition =
      TownWinCondition

  }

  override val name: String = "Witch"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Witch.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Witch") + ". You may view a center card and, if you do, you must choose a player to swap that card with. You do not view the player's card in the process."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Daybreak)

}
