
package com.mercerenies.werewolf
package game
package night

import util.Cell
import util.TextDecorator.*
import name.{NamedEntity, NamedEntityMatcher}
import response.{MessageResponse, ReplyResponse}
import board.TablePosition

import scalaz.*
import Scalaz.*

abstract class TablePositionMessageHandler extends MultipleChoiceMessageHandler[TablePosition](
  choices = TablePosition.all,
) {

  private val _playerChoice: Cell[Option[TablePosition]] = Cell(None)

  override val expectedNumbers: List[Int] = List(1)

  override val repeatsAllowed: Boolean = false

  override val noArgsMessage: String =
    "Please indicate 'left', 'middle', or 'right'."

  override def wrongNumberArgsMessage(actual: Int): String =
    "Please indicate only one of 'left', 'middle', or 'right'."

  override def repeatsDisallowedMessage(repeatedElement: TablePosition): String =
    "Repeats not allowed" // expectedNumber = 1 so this should not occur

  override def onOptionsSelected(originalMessage: String, options: List[TablePosition]): MessageResponse = {
    _playerChoice.value = Some(options.head)
    ReplyResponse("Selected " + bold(options.head.toString))
  }

  def currentChoice: Option[TablePosition] = _playerChoice.value

  def hasChoice: Boolean = currentChoice.isDefined

}
