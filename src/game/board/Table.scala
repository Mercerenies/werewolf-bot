
package com.mercerenies.werewolf
package game
package board

import name.NamedEntity

// TODO Better name for this enum.
enum Table(override val name: String) extends NamedEntity {
  case Left extends Table("Left")
  case Middle extends Table("Middle")
  case Right extends Table("Right")

  override def aliases: List[String] = Nil

}

object Table {

  val all: List[Table] = List(Table.Left, Table.Middle, Table.Right)

}
