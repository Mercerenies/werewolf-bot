
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity

trait Role extends NamedEntity {

  def baseAlignment: Alignment

}

object Role {

  val all: List[Role] = List(
    Villager,
    Werewolf,
  )

}
