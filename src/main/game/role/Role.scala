
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity
import id.UserMapping
import util.TextDecorator.*
import wincon.WinCondition

trait Role extends NamedEntity {

  type Instance <: RoleInstance

  def precedence: Int

  def baseAlignment: Alignment

  def createInstance(mapping: UserMapping): this.Instance

  def introBlurb: String

}

object Role {

  val all: List[Role] = List(
    Villager,
    Werewolf,
    Tanner,
    Seer,
  )

  def formattedList(roleList: Iterable[Role]): String =
    longCode(
      roleList.map(_.name).mkString("\n"),
    )

}
