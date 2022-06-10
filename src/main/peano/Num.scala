
package com.mercerenies.werewolf
package peano

// Not used right now, but it can never hurt to have a random
// type-level Peano arithmetic implementation in your codebase :)

sealed trait Num {
  def succ: Num.Succ[this.type] = Num.Succ(this)
}

object Num {

  sealed trait Zero extends Num
  object Zero extends Zero

  final class Succ[A <: Num](val pred: A) extends Num

  given CanonicalZero : Zero = Zero

  given CanonicalSucc[A <: Num](using x: A) : Succ[A] = Succ(x)

}
