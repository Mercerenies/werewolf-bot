
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity

trait Role extends NamedEntity {

  type Instance <: RoleInstance

  def baseAlignment: Alignment

  def createInstance(): this.Instance

}

object Role {

  val all: List[Role] = List(
    Villager,
    Werewolf,
  )

}
