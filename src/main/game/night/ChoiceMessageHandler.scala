
package com.mercerenies.werewolf
package game
package night

import util.Cell
import util.TextDecorator.*
import choice.Choice
import choice.syntax.*
import name.{NamedEntity, NamedEntityMatcher}
import response.{MessageResponse, ReplyResponse}

import scalaz.{Choice => _, *}
import Scalaz.*

trait ChoiceMessageHandler[A, B] extends NightMessageHandler {

  private val _selection: Cell[Option[A]] = Cell(None)

  def options: Choice[A]

  // Convert the Choice value (which is probably 'Either' soup) into a
  // useful ADT. An input of None means the user never responded at
  // all.
  def postprocess(input: Option[A]): B

  // The text to send when the player successfully selects an option.
  // Default implementation runs postprocess and then toString on the
  // result, but this can be overridden.
  def selectionConfirmText(input: A): String =
    "Selected " + bold(postprocess(Some(input)).toString)

  def message: String = s"Please enter ${options.blurb}"

  override def onDirectMessage(messageContents: String): MessageResponse =
    options.parse(messageContents) match {
      case Left(_) => {
        // TODO Better, more specific error messages
        ReplyResponse(message)
      }
      case Right(x) => {
        _selection.value = Some(x)
        ReplyResponse(selectionConfirmText(x))
      }
    }

  override def initialNightMessage: String =
    message

  final def currentChoice: B =
    postprocess(_selection.value)

  final def hasChoice: Boolean =
    !_selection.value.isEmpty

}
