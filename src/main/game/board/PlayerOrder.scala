
package com.mercerenies.werewolf
package game
package board

import id.{Id, UserMapping}
import role.{Role, RoleInstance}
import snapshot.BoardSnapshot

import org.javacord.api.entity.user.User

// A thin wrapper around a List of user IDs. Provides helper
// functionality for getting players relative to each other (e.g. "get
// the player to the left of X")
case class PlayerOrder(
  val players: List[Id[User]],
) {

  if (players.isEmpty) {
    throw new IllegalArgumentException("Empty player list not allowed in PlayerOrder")
  }

  export players.length

  def toList: List[Id[User]] =
    players

  def map[A](fn: (Id[User]) => A): List[A] =
    players.map(fn)

  def filter(fn: (Id[User]) => Boolean): List[Id[User]] =
    players.filter(fn)

  def indexOf(id: Id[User]): Int =
    players.indexOf(id)

}
