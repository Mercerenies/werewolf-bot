
package com.mercerenies.werewolf
package game
package role

// Roles which are grouped (werewolves and masons, mainly) use this to
// identify other members of their group.
enum GroupedRoleIdentity(val name: String) {
  // The werewolf team, visible to each other and to the Minion.
  case Werewolf extends GroupedRoleIdentity("Werewolf")
  // Masons, visible to each other.
  case Mason extends GroupedRoleIdentity("Mason")
  // Fluffy, the target of Ripper's win condition.
  case Fluffy extends GroupedRoleIdentity("Fluffy")
  // Ripper, the target of Fluffy's win condition.
  case Ripper extends GroupedRoleIdentity("Ripper")
  // Dream Wolf, identified specially to the remaining werewolves so
  // that they know who is unaware of their partners.
  case DreamWolf extends GroupedRoleIdentity("Dream Wolf")
}
