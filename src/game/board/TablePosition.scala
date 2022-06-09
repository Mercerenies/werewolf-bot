
package com.mercerenies.werewolf
package game
package board

import name.NamedEntity

enum TablePosition(override val name: String) extends NamedEntity {
  case Left extends TablePosition("Left")
  case Middle extends TablePosition("Middle")
  case Right extends TablePosition("Right")

  override def aliases: List[String] = Nil

}

object TablePosition {

  val all: List[TablePosition] = List(TablePosition.Left, TablePosition.Middle, TablePosition.Right)

}
