
package com.mercerenies.werewolf
package game
package role

case object Werewolf extends Role {

  override type Instance = RoleInstance

  override val name: String = "Werewolf"

  override val aliases: List[String] = List("WW", "Wolf")

  override val baseAlignment: Alignment = Alignment.Werewolf

  override def createInstance(): this.Instance =
    StatelessRoleInstance(this)

}
