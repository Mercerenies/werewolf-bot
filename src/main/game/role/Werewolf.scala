
package com.mercerenies.werewolf
package game
package role

import logging.Logging
import name.NoValue
import id.{Id, UserMapping}
import util.Grammar
import util.TextDecorator.*
import wincon.{WinCondition, WerewolfWinCondition}
import night.{NightMessageHandler, OptionalTablePositionMessageHandler, WerewolfMessageHandler}
import board.{Board, TablePosition}
import response.FeedbackMessage

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Werewolf extends Role {

  override class Instance(private val mapping: UserMapping) extends RoleInstance {

    import Instance.logger

    override val role: Werewolf.type = Werewolf.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    private val nightHandlerImpl: WerewolfMessageHandler =
      WerewolfMessageHandler()

    override val nightHandler: NightMessageHandler =
      nightHandlerImpl

    override def nightAction(userId: Id[User]): State[Board, FeedbackMessage] = {
      val tablePos = nightHandlerImpl.currentChoice
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

  override def createInstance(mapping: UserMapping): this.Instance =
    Werewolf.Instance(mapping)

  override val introBlurb: String =
    "You are a " + bold("Werewolf") + ". You will be informed of who the other werewolves are. If there are no other werewolves, you may look at a card in the center of the table."

  private def findWerewolfIds(board: Board): List[Id[User]] =
    board.playerRoleInstances.filter { (_, instance) => instance.seenAsWerewolf }.map { (userId, _) => userId }

}
