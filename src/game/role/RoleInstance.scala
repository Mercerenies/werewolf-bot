
package com.mercerenies.werewolf
package game
package role

import id.Id
import name.NamedEntity
import night.NightMessageHandler
import board.Board
import response.FeedbackMessage

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
trait RoleInstance {

  // Definition: An instance x of RoleInstance is said to be coherent
  // if x is of type x.role.Instance.
  val coherenceProof: this.type <:< this.role.Instance

  val nightHandler: NightMessageHandler

  val role: Role

  def nightAction(userId: Id[User]): State[Board, FeedbackMessage]

  final def downcast: this.role.Instance =
    coherenceProof(this)

}
