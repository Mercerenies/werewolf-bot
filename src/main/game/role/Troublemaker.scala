
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
        noValue :+: choiceFactory.twoPlayersNotSelfOption(initialUserId)
      ) {
        case None | Some(Left(NoValue)) => None
        case Some(Right((a, b))) => Some((a, b))
      }

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] = {
      val playerChoice = nightHandlerImpl.currentChoice
      for {
        board <- GameContext.getBoard
        message <- playerChoice match {
          case None => {
            FeedbackMessage(s"You elected to swap nobody's cards.").point[GameContext]
          }
          case Some((a, b)) => {
            val msg = FeedbackMessage(s"You have chosen to swap the cards of ${bold(a.name)} and ${bold(b.name)}.")
            val newBoard = board.swap(Position.Player(a.id), Position.Player(b.id))
            GameContext.setBoard(newBoard) >| msg
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

  override val precedence: Int = Precedence.TROUBLEMAKER

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Troublemaker.Instance(mapping, initialUserId)

  override val introBlurb: String =
    "You are the " + bold("Troublemaker") + ". You may swap two other players' cards."

}
