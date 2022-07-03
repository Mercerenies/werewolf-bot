
package com.mercerenies.werewolf
package game
package night

import util.Cell
import util.TextDecorator.*
import choice.Choice
import choice.syntax.*
import choice.formatter.ChoiceFormatter
import name.{NamedEntity, NamedEntityMatcher}
import response.{MessageResponse, ReplyResponse}
import board.Board

import scalaz.{Choice => _, *}
import Scalaz.*

trait ChoiceMessageHandler[A, B] extends NightMessageHandler {

  private val _selection: Cell[Option[A]] = Cell(None)

  def options: Choice[A]

  // Convert the Choice value (which is probably 'Either' soup) into a
  // useful ADT. An input of None means the user never responded at
  // all.
  def postprocess(input: Option[A]): B

  def selectionConfirmText(input: A): String

  def message: String = s"Please enter ${options.blurb}"

  override def onDirectMessage(board: Board, messageContents: String): MessageResponse =
    options.parse(messageContents) match {
      case Left(_) => {
        ReplyResponse(message)
      }
      case Right(x) => {
        _selection.value = Some(x)
        ReplyResponse(selectionConfirmText(x))
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

  final def currentChoice: B =
    postprocess(_selection.value)

  final def hasChoice: Boolean =
    !_selection.value.isEmpty

}

object ChoiceMessageHandler {

  private class Simple[A, B](
    override val options: Choice[A],
    private val postprocessor: (Option[A]) => B,
  )(
    using ChoiceFormatter[A],
  ) extends ChoiceMessageHandler[A, B] {

    override def postprocess(input: Option[A]): B =
      postprocessor(input)

    // The text to send when the player successfully selects an option.
    // Default implementation tries to be smart and can handle Either,
    // tuples, Lists, and things with toString.
    override def selectionConfirmText(input: A): String =
      "Selected " + bold(SelectionConfirmFormatter.format(input))

  }

  def apply[A, B](options: Choice[A])(postprocess: (Option[A]) => B)(using ChoiceFormatter[A]): ChoiceMessageHandler[A, B] =
    Simple(options, postprocess)

}
