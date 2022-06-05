
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity

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
// For roles which have no state, StatelessRoleInstance can be used.
//
// Definition: An instance x of RoleInstance is said to be coherent if
// x is of type x.role.Instance. It is not illegal to have incoherent
// role instances, but it does make event handling more difficult. In
// a game of Werewolf (and, in particular, in a Board instance), role
// instances shall always be coherent.
trait RoleInstance {

  val role: Role

}
