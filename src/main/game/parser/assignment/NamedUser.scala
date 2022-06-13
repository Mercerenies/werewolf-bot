
package com.mercerenies.werewolf
package game
package parser
package assignment

import id.Id
import name.NamedEntity

import org.javacord.api.DiscordApi
import org.javacord.api.entity.user.User
import org.javacord.api.entity.server.Server

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import scala.jdk.OptionConverters.*
import scala.concurrent.{Future, ExecutionContext}

// A user identified by their Discord ID, together with a name to
// refer to them by.
case class NamedUser(
  val id: Id[User],
  override val name: String,
  val nickname: Option[String],
) extends NamedEntity {

  override def aliases: List[String] =
    nickname.toList

}

object NamedUser {

  def fromUsers(server: Server, user: User): NamedUser =
    NamedUser(
      id = Id(user),
      name = user.getName,
      nickname = user.getNickname(server).toScala,
    )

}
