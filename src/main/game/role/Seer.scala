
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
import util.Grammar
import util.TextDecorator.*
import wincon.{WinCondition, TownWinCondition}
import night.{NightMessageHandler, OptionalTablePositionMessageHandler, SeerMessageHandler}
import board.{Board, TablePosition}
import response.FeedbackMessage

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Seer extends Role {

  override class Instance(private val mapping: UserMapping) extends RoleInstance {

    override val role: Seer.type = Seer.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl: SeerMessageHandler =
      SeerMessageHandler(mapping.toNamedUsers.toList)

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): State[Board, FeedbackMessage] = {
      import SeerMessageHandler.Choice
      val playerChoice = nightHandlerImpl.currentChoice
      for {
        board <- State.get
      } yield {
        playerChoice match {
          case Choice.NoInput | Choice.NoValue => {
            FeedbackMessage.none
          }
          case Choice.CenterCards(first, second) => {
            val firstCard = board(first).role
            val secondCard = board(second).role
            FeedbackMessage(s"The ${first.name} card is ${bold(firstCard.name)}, and the ${second.name} card is ${bold(secondCard.name)}.")
          }
          case Choice.PlayerCard(player) => {
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

  override def createInstance(mapping: UserMapping): this.Instance =
    Seer.Instance(mapping)

  override val introBlurb: String =
    "You are the " + bold("Seer") + ". You may look at two cards in the center, or one card in front of another player."

}
