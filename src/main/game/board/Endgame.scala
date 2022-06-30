
package com.mercerenies.werewolf
package game
package board

import id.Id
import role.Role
import role.instance.WerewolfRoleInstance

import org.javacord.api.entity.user.User

// This class provides a lot of seemingly-arbitrary helper functions
// intended to help evaluate people's win conditions.
final class Endgame(
  val board: Board,
  val playerOrder: PlayerOrder, // Full list of players, alive or dead, in the game's order
  val deaths: List[Id[User]],
) {

  def werewolves: List[Id[User]] =
    WerewolfRoleInstance.findWerewolfIds(board)

  def anyWerewolvesDied: Boolean =
    !(werewolves.toSet & deaths.toSet).isEmpty

  def anyoneDied: Boolean =
    !deaths.isEmpty

  def isDead(id: Id[User]): Boolean =
    deaths.contains(id)

}
