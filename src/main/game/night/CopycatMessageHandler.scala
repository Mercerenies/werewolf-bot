
package com.mercerenies.werewolf
package game
package night

import id.{Id, UserMapping}
import util.Cell
import util.TextDecorator.*
import choice.Choice
import choice.syntax.*
import choice.formatter.ChoiceFormatter
import name.{NamedEntity, NamedEntityMatcher}
import response.{MessageResponse, ReplyResponse}
import role.{Copycat, Role}
import board.{Board, TablePosition}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, Choice => _, *}
import Scalaz.{Id => _, *}

final class CopycatMessageHandler(
  private val mapping: UserMapping,
  private val initialUserId: Option[Id[User]],
  private val roleInstance: Copycat.Instance,
) extends NightMessageHandler {

  private val choiceFactory: ChoiceFactory =
    ChoiceFactory(mapping.toNamedUsers)

  private val hasCopied: Cell[Option[TablePosition]] =
    Cell(None)

  private val options: Choice[TablePosition] =
    choiceFactory.tablePosition

  def message: String =
    s"Please enter ${options.blurb}.\n\n" + bold("Note:") + " As soon as you make your selection, your choice will be committed."

  def copyRole(pos: TablePosition, role: Role): String = {
    val copiedRoleInstance = role.createInstance(mapping, initialUserId)
    roleInstance.copiedRole = Some(copiedRoleInstance)
    hasCopied.value = Some(pos)
    val viewMessage = "The " + bold(pos.name) + " card is the " + bold(role.name) + " card."
    val followupMessage = role.introBlurb + " " +  copiedRoleInstance.winCondition.blurb
    viewMessage + "\n\n" + followupMessage
  }

  override def onDirectMessage(board: Board, messageContents: String): MessageResponse = {
    options.parse(messageContents) match {
      case Left(_) => {
        ReplyResponse(message)
      }
      case Right(tablePosition) => {
        val role = board(tablePosition).role
        val message = this.copyRole(tablePosition, role)
        ReplyResponse(message)
      }
    }
  }

  override def initialNightMessage: String =
    message

  override def midnightReminder: Option[String] =
    if (hasChoice) {
      None
    } else {
      Some("Reminder: " + message)
    }

  def copiedPosition: Option[TablePosition] =
    hasCopied.value

  def hasChoice: Boolean =
    !hasCopied.value.isEmpty

}
