
package com.mercerenies.werewolf
package game
package role

import util.TextDecorator.*

case object Werewolf extends Role {

  override type Instance = RoleInstance

  override val name: String = "Werewolf"

  override val aliases: List[String] = List("WW", "Wolf")

  override val baseAlignment: Alignment = Alignment.Werewolf

  override def createInstance(): this.Instance =
    StatelessRoleInstance(this)

  // TODO Choosing a center card if you're the only one.
  override val introMessage: String =
    "You are a " + bold("Werewolf") + ". You will be informed of who the other werewolves are. If there are no other werewolves, you may look at a card in the center of the table."

}
