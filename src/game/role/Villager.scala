
package com.mercerenies.werewolf
package game
package role

case object Villager extends Role {

  override type Instance = RoleInstance

  override val name: String = "Villager"

  override val aliases: List[String] = List("Townie", "Town", "Vanilla")

  override val baseAlignment: Alignment = Alignment.Town

  override def createInstance(): this.Instance =
    StatelessRoleInstance(this)

}