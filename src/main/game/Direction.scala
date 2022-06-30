
package com.mercerenies.werewolf
package game

import name.NamedEntity

import scala.math.Ordering

enum Direction(override val name: String) extends NamedEntity {
  case Left extends Direction("Left")
  case Right extends Direction("Right")

  override def aliases: List[String] = Nil

  def other: Direction =
    this match {
      case Left => Right
      case Right => Left
    }

}

object Direction {

  val all: List[Direction] = List(Direction.Left, Direction.Right)

}
