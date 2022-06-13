
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity
import util.TextDecorator.*
import wincon.WinCondition

trait Role extends NamedEntity {

  type Instance <: RoleInstance

  def precedence: Int

  def baseAlignment: Alignment

  def createInstance(): this.Instance

  def introBlurb: String

}

object Role {

  val all: List[Role] = List(
    Villager,
    Werewolf,
  )

  def formattedList(roleList: Iterable[Role]): String =
    longCode(
      roleList.map(_.name).mkString("\n"),
    )

}
