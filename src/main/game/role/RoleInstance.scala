
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import name.NamedEntity
import night.NightMessageHandler
import board.Board
import response.FeedbackMessage
import wincon.WinCondition

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

// A Role is an immutable object describing the properties and rules
// associated with a One Night Ultimate Werewolf role. A Role object
// never changes. A RoleInstance contains the particular state
// associated with a particular role and *does* change over the course
// of a game.
//
// For instance, the Doppelganger role object contains details about
// how the role is meant to be played and what prompts to show the
// user. But its corresponding RoleInstance will store *which* role
// the doppelganger has copied.
//
// Note: Generally speaking, RoleInstance objects should always have
// the default equals and hashCode from AnyRef (i.e. they should
// compare for pointer equality) since these objects are intended to
// be mutated in-place.
trait RoleInstance {

  // Definition: An instance x of RoleInstance is said to be coherent
  // if x is of type x.role.Instance.
  val coherenceProof: this.type <:< this.role.Instance

  val nightHandler: NightMessageHandler

  val role: Role

  def nightAction(userId: Id[User]): State[Board, FeedbackMessage]

  def winCondition: WinCondition

  // If this is true, then other werewolves (and the minion) will see
  // this role when they get to see who the werewolf team is.
  def seenAsWerewolf: Boolean = false

  final def downcast: this.role.Instance =
    coherenceProof(this)

  final def fullIntroMessage(username: String): String =
    username + ",\n\n" +
      role.introBlurb + " " + winCondition.blurb + "\n" +
      "Copying or screenshotting any part of this message is against the rules. Good luck!"

}

object RoleInstance {

  // A helper method for a common pattern. In nightAction, it's common
  // to check some input that the user provided over the past night
  // phase. Ideally, all users have supplied the requisite input, but
  // sometimes people forget.
  //
  // withForgottenInput takes the Option containing user input (if
  // supplied), a default value, and a message. It also takes a
  // function to apply to the input value. If the user supplied a
  // value (i.e. the Option is not None), then that value is passed
  // harmlessly to the given callback function. If the user did not
  // supply an input value, then defaultValue is used instead, and
  // defaultMessage is prepended to the user's feedback.
  // defaultMessage should be a message indicating to the user which
  // option was chosen for them, to be completely unambiguous.
  //
  // Example usage:
  //
  //   withForgottenInput(userInput, TablePosition.Left, "(Defaulting to LEFT table position)") { input =>
  //     ...
  //   }
  def withForgottenInput[A, F[_]: Functor](input: Option[A], defaultValue: A, defaultMessage: String)(fn: (A) => F[FeedbackMessage]): F[FeedbackMessage] = {
    val defaultFeedback = FeedbackMessage(defaultMessage)
    input match {
      case None => fn(defaultValue).map { defaultFeedback ++ _ }
      case Some(x) => fn(x)
    }
  }

}
