
package com.mercerenies.werewolf
package game
package role

trait Role {

  def name: String

  def aliases: List[String]

  def baseAlignment: Alignment

}

object Role {

  val all: List[Role] = List(
    Villager,
    Werewolf,
  )

}
