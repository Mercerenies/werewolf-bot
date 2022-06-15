
package com.mercerenies.werewolf
package peano

sealed trait Num {
  def succ: Num.Succ[this.type] = Num.Succ(this)
  def toInt: Int
}

object Num {

  sealed trait Zero extends Num
  object Zero extends Zero {
    val toInt: Int = 0
  }

  case class Succ[A <: Num](val pred: A) extends Num {
    val toInt: Int = pred.toInt + 1
  }

  given CanonicalZero : Zero = Zero

  given CanonicalSucc[A <: Num](using x: A) : Succ[A] = Succ(x)

  val One = Succ(Zero)
  val Two = Succ(One)
  val Three = Succ(Two)
  val Four = Succ(Three)
  val Five = Succ(Four)

}
