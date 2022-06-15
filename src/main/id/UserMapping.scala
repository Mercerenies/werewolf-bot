
package com.mercerenies.werewolf
package id

import Ids.*
import game.parser.assignment.NamedUser

import org.javacord.api.DiscordApi
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import java.util.NoSuchElementException

import scala.concurrent.{Future, ExecutionContext}

trait UserMapping {

  def keys: Iterable[Id[User]]

  def get(id: Id[User]): Option[User]

  def getName(id: Id[User]): Option[String]

  final def apply(id: Id[User]): User =
    get(id) match {
      case None => throw new NoSuchElementException(s"No such element ${id} in UserMapping.apply")
      case Some(x) => x
    }

  final def nameOf(id: Id[User]): String =
    getName(id) match {
      case None => throw new NoSuchElementException(s"No such element ${id} in UserMapping.nameOf")
      case Some(x) => x
    }

  // Note: The default implementation works, but it can be overridden
  // to allow nicknames to be used as well.
  def toNamedUsers: Iterable[NamedUser] =
    keys.map { id => NamedUser(id, nameOf(id), None) }

}

object UserMapping {

  private class FromMap(val map: Map[Id[User], User]) extends UserMapping {
    export map.{get, keys}

    override def getName(id: Id[User]): Option[String] =
      get(id).map { _.getName }

  }

  private class FromServer(val map: Map[Id[User], User], val server: Server) extends UserMapping {
    export map.{get, keys}

    override def getName(id: Id[User]): Option[String] =
      get(id).map { _.getDisplayName(server) }

    override def toNamedUsers: Iterable[NamedUser] =
      keys.map { id => NamedUser(id, apply(id).getName, Some(nameOf(id))) }

  }

  def fromMap(map: Map[Id[User], User]): UserMapping =
    FromMap(map)

  def fromServer(api: DiscordApi, server: Server, idsOfInterest: List[Id[User]])(using ExecutionContext): Future[UserMapping] =
    for {
      users <- idsOfInterest.traverse { id =>
        api.getUser(id).map { (id, _) }
      }
    } yield {
      FromServer(users.toMap, server)
    }

}
