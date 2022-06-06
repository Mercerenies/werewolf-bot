
package com.mercerenies.werewolf
package game
package name

// A string as a name with no aliases.
case class SimpleName(override val name: String) extends NamedEntity {

  def aliases: List[String] = Nil

}
