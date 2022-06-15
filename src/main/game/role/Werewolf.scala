
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
import util.Grammar
import util.TextDecorator.*
import wincon.{WinCondition, WerewolfWinCondition}
import night.{NightMessageHandler, OptionalTablePositionMessageHandler}
import board.{Board, TablePosition}
import response.FeedbackMessage

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Werewolf extends Role {

  class MessageHandler extends OptionalTablePositionMessageHandler {

    override val initialNightMessage: String =
      bold("Please reply 'left', 'middle', 'right', or 'none'") + " to indicate the card you will look at if you're the lone werewolf."

    override def midnightReminder: Option[String] =
      if (hasChoice) {
        None
      } else {
        Some(
          "Reminder: Please indicate the card you will look at if you're the lone werewolf, by " +
            bold("replying 'left', 'middle', 'right', or 'none'."),
        )
      }

  }

  override class Instance extends RoleInstance {

    import Instance.logger

    override val role: Werewolf.type = Werewolf.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val tablePositionMessageHandler =
      MessageHandler()

    override val nightHandler: NightMessageHandler =
      tablePositionMessageHandler

    override def nightAction(mapping: UserMapping, userId: Id[User]): State[Board, FeedbackMessage] = {
      val tablePos = tablePositionMessageHandler.currentChoice
      for {
        board <- State.get
      } yield {
        val werewolfIds = findWerewolfIds(board)
        if (werewolfIds.length <= 1) {

          // Sanity check; I sincerely hope this never triggers.
          if (werewolfIds.length < 1) {
            logger.warn(s"Werewolf ${userId} is trying to view the werewolf list but there are no werewolves")
          }

          // There's one werewolf, so look at the center card.
          tablePos match {
            case NoValue => {
              FeedbackMessage.none
            }
            case tablePos: TablePosition => {
              val centerCard = board(tablePos).role

              FeedbackMessage("You are the " + bold("solo werewolf") + ". The " + bold(tablePos.toString) + " card is " + bold(centerCard.name) + ".")
            }
          }
        } else {
          val werewolfNames = werewolfIds.map { mapping.nameOf(_) }.sorted
          val werewolfNamesList = Grammar.conjunctionList(werewolfNames)
          FeedbackMessage("The werewolf team consists of " + bold(werewolfNamesList) + ".")
        }
      }
    }

    override val winCondition: WinCondition =
      WerewolfWinCondition

    override val seenAsWerewolf: Boolean = true

  }

  object Instance extends Logging[Instance]

  override val name: String = "Werewolf"

  override val aliases: List[String] = List("WW", "Wolf")

  override val baseAlignment: Alignment = Alignment.Werewolf

  override val precedence: Int = Precedence.WEREWOLF

  override def createInstance(): this.Instance =
    Werewolf.Instance()

  override val introBlurb: String =
    "You are a " + bold("Werewolf") + ". You will be informed of who the other werewolves are. If there are no other werewolves, you may look at a card in the center of the table."

  private def findWerewolfIds(board: Board): List[Id[User]] =
    board.playerRoleInstances.filter { (_, instance) => instance.seenAsWerewolf }.map { (userId, _) => userId }

}
