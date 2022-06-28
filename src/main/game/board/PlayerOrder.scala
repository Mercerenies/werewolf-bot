
package com.mercerenies.werewolf
package game
package board

import id.{Id, UserMapping}
import role.{Role, RoleInstance}
import snapshot.BoardSnapshot

import org.javacord.api.entity.user.User

case class PlayerOrder(
  val players: List[Id[User]],
) {

  export players.{indexOf, length}

  def toList: List[Id[User]] =
    players

  def map[A](fn: (Id[User]) => A): List[A] =
    players.map(fn)

  def filter(fn: (Id[User]) => Boolean): List[Id[User]] =
    players.filter(fn)

}
