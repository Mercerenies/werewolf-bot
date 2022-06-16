
package com.mercerenies.werewolf
package game
package night

import name.{NamedEntity, NamedEntityMatcher}
import response.{MessageResponse, ReplyResponse}

import scalaz.*
import Scalaz.*

@deprecated /////
trait MultipleChoiceMessageHandler[A <: NamedEntity](
  val choices: List[A],
) extends NightMessageHandler {

  private val matcher: NamedEntityMatcher[A] =
    NamedEntity.matcher(choices)

  // The acceptable number of choices the user can input. Must be
  // values greater than 0.
  def expectedNumbers: List[Int]

  // Whether the user is allowed to choose the same thing multiple
  // times.
  def repeatsAllowed: Boolean

  def noArgsMessage: String

  def wrongNumberArgsMessage(actual: Int): String

  def repeatsDisallowedMessage(repeatedElement: A): String

  final def validateSelection(selection: List[A]): String \/ Unit =
    if (selection.length == 0) {
      noArgsMessage.left
    } else if (!expectedNumbers.contains(selection.length)) {
      wrongNumberArgsMessage(selection.length).left
    } else {
      util.findDuplicate(selection) match {
        case Some(dup) if !repeatsAllowed => {
          repeatsDisallowedMessage(dup).left
        }
        case _ => {
          ().right
        }
      }
    }

  // Precondition: This method shall only be called if both of the
  // following are true.
  //
  // (1) options.length == expectedNumber
  //
  // (2) options contains no duplicates OR repeatsAllowed is true
  def onOptionsSelected(originalMessage: String, options: List[A]): MessageResponse

  override def onDirectMessage(messageContents: String): MessageResponse = {
    val matches = matcher.findAll(messageContents).toList
    validateSelection(matches) match {
      case -\/(err) => ReplyResponse(err)
      case \/-(()) => onOptionsSelected(messageContents, matches)
    }
  }

}
