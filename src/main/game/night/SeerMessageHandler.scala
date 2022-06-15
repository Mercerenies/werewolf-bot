
package com.mercerenies.werewolf
package game
package night

import util.Cell
import util.TextDecorator.*
import name.{NamedEntity, NamedEntityMatcher, NoValue}
import response.{MessageResponse, ReplyResponse}
import parser.assignment.{NamedPosition, NamedUser}
import board.TablePosition

import scalaz.*
import Scalaz.*

class SeerMessageHandler(
  users: List[NamedUser],
) extends MultipleChoiceMessageHandler[NamedPosition | NoValue](
  choices = users.map { NamedPosition.Player(_) } ++ TablePosition.all.map { NamedPosition.Table(_) },
) {

  import SeerMessageHandler.Choice

  private val _playerChoice: Cell[Choice] = Cell(Choice.NoInput)

  override val expectedNumbers: List[Int] = List(1, 2)

  override val repeatsAllowed: Boolean = false

  val genericMessage: String =
    "Please indicate the two table positions you wish to look at (out of 'Left', 'Middle', or 'Right'), or the name of the player whose card you wish to see."

  override val noArgsMessage: String =
    genericMessage

  override def initialNightMessage: String =
    bold(genericMessage)

  override def midnightReminder: Option[String] =
    if (hasChoice) {
      None
    } else {
      Some("Reminder: " + bold(genericMessage))
    }

  override def wrongNumberArgsMessage(actual: Int): String =
    "Please indicate either two table positions or one player name."

  override def repeatsDisallowedMessage(repeatedElement: NamedPosition | NoValue): String =
    s"You can't look at the same card '${repeatedElement}' twice."

  override def onOptionsSelected(originalMessage: String, options: List[NamedPosition | NoValue]): MessageResponse =
    Choice.fromInputList(options) match {
      case None => ReplyResponse(genericMessage)
      case Some(x) => {
        _playerChoice.value = x
        ReplyResponse("Selected " + bold(x.toUserString))
      }
    }

  def currentChoice: Choice = _playerChoice.value

  def hasChoice: Boolean = _playerChoice.value.isDefined

}

object SeerMessageHandler {

  enum Choice {
    case NoInput // Seer has not chosen a value at all.
    case NoValue // Seer explicitly asked to see no cards.
    case CenterCards(val first: TablePosition, val second: TablePosition)
    case PlayerCard(val player: NamedUser)

    def isDefined: Boolean = (this != NoInput)

    def toUserString: String =
      this match {
        case NoInput | NoValue => "None"
        case CenterCards(a, b) => s"${a.name} and ${b.name}"
        case PlayerCard(p) => p.name
      }

  }

  object Choice {

    def fromInputList(options: List[NamedPosition | NoValue]): Option[Choice] =
      options match {
        case List(name.NoValue) => Some(NoValue)
        case List(NamedPosition.Player(user)) => Some(PlayerCard(user))
        case List(NamedPosition.Table(first), NamedPosition.Table(second)) => Some(CenterCards(first, second))
        case _ => None
      }

  }

}
