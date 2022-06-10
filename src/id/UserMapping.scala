
package com.mercerenies.werewolf
package id

import Ids.*

import org.javacord.api.DiscordApi
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import java.util.NoSuchElementException

import scala.concurrent.{Future, ExecutionContext}

trait UserMapping {

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

}

object UserMapping {

  private class FromMap(val map: Map[Id[User], User]) extends UserMapping {
    export map.get

    override def getName(id: Id[User]): Option[String] =
      get(id).map { _.getName }

  }

  private class FromNameMap(val map: Map[Id[User], User], val nameMap: Map[Id[User], String]) extends UserMapping {
    export map.get

    override def getName(id: Id[User]): Option[String] =
      nameMap.get(id)

  }

  def fromMap(map: Map[Id[User], User]): UserMapping =
    FromMap(map)

  def fromNameMap(map: Map[Id[User], User], nameMap: Map[Id[User], String]): UserMapping =
    FromNameMap(map, nameMap)

  def fromServer(api: DiscordApi, server: Server, idsOfInterest: List[Id[User]])(using ExecutionContext): Future[UserMapping] =
    for {
      users <- idsOfInterest.traverse { id =>
        api.getUser(id).map { (id, _) }
      }
    } yield {
      val usersMap = users.toMap
      val namesMap = usersMap.view.mapValues { _.getDisplayName(server) }.toMap
      fromNameMap(usersMap, namesMap)
    }

}
