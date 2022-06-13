
package com.mercerenies.werewolf
package game
package board

import id.Id
import role.{Role, RoleInstance}

import org.javacord.api.entity.user.User

final class Endgame(
  val board: Board,
  val deaths: List[Id[User]],
)
