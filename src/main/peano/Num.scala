
package com.mercerenies.werewolf
package peano

// Not used right now, but it can never hurt to have a random
// type-level Peano arithmetic implementation in your codebase :)

sealed trait Num {
  def succ: Num.Succ[this.type] = Num.Succ(this)
  def toInt: Int
}

object Num {

  sealed trait Zero extends Num
  object Zero extends Zero {
    val toInt: Int = 0
  }

  final class Succ[A <: Num](val pred: A) extends Num {
    val toInt: Int = pred.toInt + 1
  }

  given CanonicalZero : Zero = Zero

  given CanonicalSucc[A <: Num](using x: A) : Succ[A] = Succ(x)

}
