
package com.mercerenies.werewolf
package game
package role

import id.Id
import util.TextDecorator.*
import wincon.{WinCondition, WerewolfWinCondition}
import night.{NightMessageHandler, TablePositionMessageHandler}
import board.{Board, TablePosition}
import response.FeedbackMessage

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

case object Werewolf extends Role {

  class MessageHandler extends TablePositionMessageHandler {

    override val initialNightMessage: String =
      bold("Please reply 'left', 'middle', or 'right'") + " to indicate the card you will look at if you're the lone werewolf."

    override def midnightReminder: Option[String] =
      if (hasChoice) {
        None
      } else {
        Some(
          "Reminder: Please indicate the card you will look at if you're the lone werewolf, by " +
            bold("replying 'left', 'middle', or 'right'."),
        )
      }

  }

  override class Instance extends RoleInstance {

    override val role: Werewolf.type = Werewolf.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val tablePositionMessageHandler =
      MessageHandler()

    override val nightHandler: NightMessageHandler =
      tablePositionMessageHandler

    private val forgottenInputMessage: String =
      "(Defaulting to the " + bold("Left") + " card)"

    override def nightAction(userId: Id[User]): State[Board, FeedbackMessage] =
      RoleInstance.withForgottenInput(tablePositionMessageHandler.currentChoice, TablePosition.Left, forgottenInputMessage) { tablePos =>
        for {
          board <- State.get
        } yield {
          ??? /////
        }
      }

  }

  override val name: String = "Werewolf"

  override val aliases: List[String] = List("WW", "Wolf")

  override val baseAlignment: Alignment = Alignment.Werewolf

  override val seenAsWerewolf: Boolean = true

  override def createInstance(): this.Instance =
    Werewolf.Instance()

  // TODO Choosing a center card if you're the only one.
  override val introBlurb: String =
    "You are a " + bold("Werewolf") + ". You will be informed of who the other werewolves are. If there are no other werewolves, you may look at a card in the center of the table."

  override val winCondition: WinCondition =
    WerewolfWinCondition

}
