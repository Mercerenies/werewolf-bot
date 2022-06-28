
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import name.NamedEntity
import night.NightMessageHandler
import board.Board
import board.snapshot.{RoleSnapshot, SimpleRoleSnapshot}
import response.FeedbackMessage
import wincon.WinCondition
import context.GameContext
import votes.context.VotesContext

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

  def votesPrecedence: Int = VotesPrecedence.NO_ACTION

  def nightAction(userId: Id[User]): GameContext[FeedbackMessage]

  def winCondition: WinCondition

  // Grouped roles, such as werewolves and masons, use this to
  // identify the other members of their group. Anybody who should
  // appear as a "werewolf" to other werewolves or to the minion, for
  // instance, should list GroupedRoleIdentity.Werewolf in this list,
  // including doppelgangers who happened to copy werewolves.
  def seenAs: List[GroupedRoleIdentity] = Nil

  // The action taken after all players have placed their votes but
  // before the game has been decided. This applies to roles like
  // Hunter and Bodyguard who manipulate the death list. This function
  // should return true if any modifications were made to the death
  // roster and false otherwise. Since the vast majority of roles do
  // not have a votePhaseAction, the default implementation does
  // nothing and returns false.
  //
  // WARNING: Subclasses which override this should use caution.
  // VotesContext makes some fairly strong guarantees itself about
  // which roster states are reachable from which ones (see the
  // documentation in that file for details), and this method makes a
  // stronger guarantee on top of that. votePhaseAction should be
  // idempotent in a very strong sense. That is, suppose
  // votePhaseAction(userId) is called once, then some arbitrary
  // sequence of VotesContext actions is performed (subject to the
  // assumptions in VotesContext), then votePhaseAction(userId) is
  // called a second time. If the first call returned true, then the
  // second call must perform no actions.
  def votePhaseAction(userId: Id[User]): VotesContext[Boolean] =
    false.point

  // An immutable snapshot of this role's state at a given moment, for
  // the purposes of creating the game's records at the end. For roles
  // that do not have state, the default implementation suffices.
  def toSnapshot: RoleSnapshot = SimpleRoleSnapshot(role)

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
  @deprecated
  def withForgottenInput[A, F[_]: Functor](input: Option[A], defaultValue: A, defaultMessage: String)(fn: (A) => F[FeedbackMessage]): F[FeedbackMessage] = {
    val defaultFeedback = FeedbackMessage(defaultMessage)
    input match {
      case None => fn(defaultValue).map { defaultFeedback ++ _ }
      case Some(x) => fn(x)
    }
  }

}
