
package com.mercerenies.werewolf
package game
package board

import id.Id
import role.{Role, RoleInstance}

import org.javacord.api.entity.user.User

// This class provides a lot of seemingly-arbitrary helper functions
// intended to help evaluate people's win conditions.
final class Endgame(
  val board: Board,
  val playerList: List[Id[User]], // Full list of players, alive or dead, in the game's order
  val deaths: List[Id[User]],
) {

  val werewolves: List[Id[User]] =
    board.playerRoleInstances.filter { (_, instance) => instance.seenAsWerewolf }.map { (userId, _) => userId }

  def anyWerewolvesDied: Boolean =
    !(werewolves.toSet & deaths.toSet).isEmpty

  def anyoneDied: Boolean =
    !deaths.isEmpty

}
