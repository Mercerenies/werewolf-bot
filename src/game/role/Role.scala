
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity
import util.TextDecorator.*
import wincon.WinCondition

trait Role extends NamedEntity {

  type Instance <: RoleInstance

  def baseAlignment: Alignment

  def createInstance(): this.Instance

  def introBlurb: String

  def winCondition: WinCondition

  final def fullIntroMessage(username: String): String =
    username + ",\n\n" +
      introBlurb + " " + winCondition.blurb + "\n" +
      "Copying or screenshotting any part of this message is against the rules. Good luck!"

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
