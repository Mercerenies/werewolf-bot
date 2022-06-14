
package com.mercerenies.werewolf
package game
package board

import name.NamedEntity

enum TablePosition(override val name: String, override val aliases: List[String] = Nil) extends NamedEntity {
  case Left extends TablePosition("Left")
  case Middle extends TablePosition("Middle", List("Center"))
  case Right extends TablePosition("Right")

}

object TablePosition {

  val all: List[TablePosition] = List(TablePosition.Left, TablePosition.Middle, TablePosition.Right)

}
