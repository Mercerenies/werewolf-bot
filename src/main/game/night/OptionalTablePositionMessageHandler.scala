
package com.mercerenies.werewolf
package game
package night

import util.Cell
import util.TextDecorator.*
import name.{NamedEntity, NamedEntityMatcher, NoValue}
import response.{MessageResponse, ReplyResponse}
import board.TablePosition

import scalaz.*
import Scalaz.*

abstract class OptionalTablePositionMessageHandler extends MultipleChoiceMessageHandler[TablePosition | NoValue](
  choices = NoValue :: TablePosition.all,
) {

  // Minor internal distinction: If this cell contains None, then the
  // user has given no input. In that case, currentChoice still
  // returns NoValue, but hasChoice will return false. This allows the
  // reminder night message to still go through on no input, but it
  // will not go through if the user explicitly said 'none'.
  private val _playerChoice: Cell[Option[TablePosition | NoValue]] = Cell(None)

  override val expectedNumbers: List[Int] = List(1)

  override val repeatsAllowed: Boolean = false

  override val noArgsMessage: String =
    "Please indicate 'left', 'middle', 'right', or 'none'."

  override def wrongNumberArgsMessage(actual: Int): String =
    "Please indicate only one of 'left', 'middle', 'right', or 'none'."

  override def repeatsDisallowedMessage(repeatedElement: TablePosition | NoValue): String =
    "Repeats not allowed" // expectedNumber = 1 so this should not occur

  override def onOptionsSelected(originalMessage: String, options: List[TablePosition | NoValue]): MessageResponse = {
    _playerChoice.value = Some(options.head)
    ReplyResponse("Selected " + bold(options.head.toString))
  }

  def currentChoice: TablePosition | NoValue = _playerChoice.value.getOrElse(NoValue)

  def hasChoice: Boolean = _playerChoice.value.isDefined

}
