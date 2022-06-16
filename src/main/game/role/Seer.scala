
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
import board.{Board, TablePosition}
import response.FeedbackMessage
import choice.syntax.*
import parser.assignment.NamedUser
import context.GameContext

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Seer extends Role {

  enum UserChoice {
    case None
    case CenterCards(val first: TablePosition, val second: TablePosition)
    case PlayerCard(val player: NamedUser)
  }

  override class Instance(
    private val mapping: UserMapping,
    private val initialUserId: Option[Id[User]],
  ) extends RoleInstance {

    private val choiceFactory = ChoiceFactory(mapping.toNamedUsers)

    override val role: Seer.type = Seer.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl =
      ChoiceMessageHandler(
        noValue :+: choiceFactory.twoTablePositions :+: choiceFactory.playerNotSelfOption(initialUserId)
      ) {
        case None | Some(Left(NoValue)) => UserChoice.None
        case Some(Right(Left((a, b)))) => UserChoice.CenterCards(a, b)
        case Some(Right(Right(p))) => UserChoice.PlayerCard(p)
      }

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] = {
      val playerChoice = nightHandlerImpl.currentChoice
      for {
        board <- GameContext.getBoard
      } yield {
        playerChoice match {
          case UserChoice.None => {
            FeedbackMessage("You elected not to look at any cards.")
          }
          case UserChoice.CenterCards(first, second) => {
            val firstCard = board(first).role
            val secondCard = board(second).role
            FeedbackMessage(s"The ${first.name} card is ${bold(firstCard.name)}, and the ${second.name} card is ${bold(secondCard.name)}.")
          }
          case UserChoice.PlayerCard(player) => {
            val card = board(player.id).role
            FeedbackMessage(s"The card in front of ${player.displayName} is " + bold(card.name) + ".")
          }
        }
      }
    }

    override val winCondition: WinCondition =
      TownWinCondition

  }

  override val name: String = "Seer"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val precedence: Int = Precedence.SEER

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Seer.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Seer") + ". You may look at two cards in the center, or one card in front of another player."

}
