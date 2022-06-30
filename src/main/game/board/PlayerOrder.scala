
package com.mercerenies.werewolf
package game
package board

import id.{Id, UserMapping}
import role.Role
import snapshot.BoardSnapshot

import org.javacord.api.entity.user.User

import scala.math.floorMod

// A thin wrapper around a List of user IDs. Provides helper
// functionality for getting players relative to each other (e.g. "get
// the player to the left of X")
case class PlayerOrder(
  val players: List[Id[User]],
) {

  if (players.isEmpty) {
    throw new IllegalArgumentException("Empty player list not allowed in PlayerOrder")
  }

  if (players.length != players.toSet.size) {
    throw new IllegalArgumentException("Duplicate players not allowed in PlayerOrder")
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

  def indexOfChecked(id: Id[User]): Int = {
    val result = indexOf(id)
    if (result < 0) {
      throw new NoSuchElementException(s"Could not find ${id} in ${players}")
    } else {
      result
    }
  }

  private def getWrapped(index: Int): Id[User] =
    players(floorMod(index, players.length))

  def leftOf(id: Id[User]): Id[User] =
    getWrapped(indexOfChecked(id) - 1)

  def rightOf(id: Id[User]): Id[User] =
    getWrapped(indexOfChecked(id) + 1)

  def sideOf(id: Id[User], dir: Direction): Id[User] =
    dir match {
      case Direction.Left => leftOf(id)
      case Direction.Right => rightOf(id)
    }

  def adjacentPlayers(id: Id[User]): (Id[User], Id[User]) =
    (leftOf(id), rightOf(id))

}
