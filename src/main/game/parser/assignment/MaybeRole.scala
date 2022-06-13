
package com.mercerenies.werewolf
package game
package parser
package assignment

import id.Id
import id.Ids.*
import name.NamedEntity
import board.{Board, Position, TablePosition}
import role.Role

import org.javacord.api.DiscordApi
import org.javacord.api.entity.user.User
import org.javacord.api.entity.server.Server

// Isomorphic to Option[Role], but this trait implements NamedEntity
// (with None being named "None" or "Unknown") and hence can be parsed
// for.
case class MaybeRole(val value: Option[Role]) extends NamedEntity {

  def toOption: Option[Role] = value

  override def name: String = value match {
    case None => "None"
    case Some(role) => role.name
  }

  override def aliases: List[String] = value match {
    case None => List("Unknown")
    case Some(role) => role.aliases
  }

}

object MaybeRole {

  def fromOption(opt: Option[Role]): MaybeRole =
    MaybeRole(opt)

  def fromRole(role: Role): MaybeRole =
    MaybeRole(Some(role))

  val empty: MaybeRole =
    MaybeRole(None)

}
