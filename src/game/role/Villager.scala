
package com.mercerenies.werewolf
package game
package role

object Villager extends Role {

  override val name: String = "Villager"

  override val aliases: List[String] = List("Townie", "Town", "Vanilla")

  override val baseAlignment: Alignment = Alignment.Town

}
