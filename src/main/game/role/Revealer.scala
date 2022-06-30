
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

object Revealer extends Role {

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends RoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Revealer.type = Revealer.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        (noValue :+: choiceFactory.playerNotSelfOption(initialUserId)).formattedList
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right(player)) => Some(player)
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
                t("chose not to reveal any cards.")
              })
            } yield {
              FeedbackMessage(s"You elected to reveal no cards.")
            }
          }
          case Some(player) => {
            val role = board(player.id).role
            val shouldReveal = (role.baseAlignment == Alignment.Town)
            for {
              _ <- whenM(shouldReveal) { GameContext.revealCard(Position.Player(player.id)) }
              _ <- if (shouldReveal) {
                GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                  t("revealed the card in front of ")
                  playerName(player.id)
                  t(", which was ")
                  roleName(role)
                  t(".")
                })
              } else {
                GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                  t("looked at the card in front of ")
                  playerName(player.id)
                  t(", which was ")
                  roleName(role)
                  t(". The card was flipped back over, as it is not a town card.")
                })
              }
            } yield {
              if (shouldReveal) {
                FeedbackMessage(s"You flipped over the card in front of ${bold(player.name)}, revealing that it was ${bold(role.name)}.")
              } else {
                FeedbackMessage(s"You saw the card in front of ${bold(player.name)}, revealing that it was ${bold(role.name)}. This card is not town-aligned, so you flipped it back over.")
              }
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

  override val name: String = "Revealer"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.REVEALER

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Revealer.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Revealer") + ". You may choose to look at a card in front of a player. If that card is town-aligned, the card remains face-up and will be revealed publicly at day start."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Daybreak)

}
