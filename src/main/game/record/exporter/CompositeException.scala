
package com.mercerenies.werewolf
package game
package record
package exporter

class CompositeException(
  val errors: List[Throwable],
) extends Exception {

  override def toString: String =
    "CompositeException:\n" + errors.map(_.toString).mkString("\n")

}
