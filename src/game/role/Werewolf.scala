
package com.mercerenies.werewolf
package game
package role

object Werewolf extends Role {

  override val name: String = "Werewolf"

  override val aliases: List[String] = List("WW", "Wolf")

  override val baseAlignment: Alignment = Alignment.Werewolf

}
