
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity
import util.TextDecorator.*
import wincon.WinCondition

trait Role extends NamedEntity {

  type Instance <: RoleInstance

  def baseAlignment: Alignment

  // If this is true, then other werewolves (and the minion) will see
  // this role when they get to see who the werewolf team is.
  def seenAsWerewolf: Boolean = false

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
