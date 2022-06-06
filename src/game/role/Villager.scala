
package com.mercerenies.werewolf
package game
package role

import util.TextDecorator.*
import wincon.{WinCondition, TownWinCondition}

case object Villager extends Role {

  override type Instance = RoleInstance

  override val name: String = "Villager"

  override val aliases: List[String] = List("Townie", "Town", "Vanilla")

  override val baseAlignment: Alignment = Alignment.Town

  override def createInstance(): this.Instance =
    StatelessRoleInstance(this)

  override val introBlurb: String =
    "You are a " + bold("Villager") + ". You have no special abilities to activate."

  override val winCondition: WinCondition =
    TownWinCondition

}
