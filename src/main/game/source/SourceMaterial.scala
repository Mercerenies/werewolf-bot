
package com.mercerenies.werewolf
package game
package source

case class SourceMaterial(
  val name: String,
)

object SourceMaterial {
  val Werewolf = SourceMaterial("One Night Ultimate Werewolf")
  val Daybreak = SourceMaterial("One Night Ultimate Werewolf Daybreak")
  val Vampire = SourceMaterial("One Night Ultimate Vampire")
  val Bonus = SourceMaterial("One Night Bonus Roles")
  val Alien = SourceMaterial("One Night Ultimate Alien")
}
