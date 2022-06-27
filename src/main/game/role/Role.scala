
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity
import id.{Id, UserMapping}
import util.TextDecorator.*
import wincon.WinCondition

import org.javacord.api.entity.user.User

trait Role extends NamedEntity {

  type Instance <: RoleInstance

  def precedence: Int

  def baseAlignment: Alignment

  def createInstance(mapping: UserMapping, initialUser: Option[Id[User]]): this.Instance

  def introBlurb: String

}

object Role {

  val all: List[Role] = List(
    DreamWolf,
    Drunk,
    Exposer,
    Insomniac,
    Mason,
    Robber,
    Seer,
    Tanner,
    Troublemaker,
    Villager,
    Werewolf,
    Witch,
  )

  def formattedList(roleList: Iterable[Role]): String =
    longCode(
      roleList.map(_.name).mkString("\n"),
    )

}
