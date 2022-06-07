
package com.mercerenies.werewolf
package game
package night

import util.TextDecorator.*
import name.{NamedEntity, NamedEntityMatcher}
import response.{MessageResponse, ReplyResponse}
import board.Table

import scalaz.*
import Scalaz.*

class TablePositionMessageHandler(
  override val initialNightMessage: String
) extends MultipleChoiceMessageHandler[Table](
  choices = Table.all,
) {

  private var _playerChoice: Option[Table] = None

  override val expectedNumber: Int = 1

  override val repeatsAllowed: Boolean = false

  override val noArgsMessage: String =
    "Please indicate 'left', 'middle', or 'right'."

  override def wrongNumberArgsMessage(expected: Int, actual: Int): String =
    "Please indicate only one of 'left', 'middle', or 'right'."

  override def repeatsDisallowedMessage(repeatedElement: Table): String =
    "Repeats not allowed" // expectedNumber = 1 so this should not occur

  override def onOptionsSelected(originalMessage: String, options: List[Table]): MessageResponse = {
    _playerChoice = Some(options.head)
    ReplyResponse("Selected " + bold(options.head.toString))
  }

}
