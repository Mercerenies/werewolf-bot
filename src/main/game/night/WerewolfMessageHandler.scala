
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

class WerewolfMessageHandler extends OptionalTablePositionMessageHandler {

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
