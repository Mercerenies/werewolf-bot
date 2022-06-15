
package com.mercerenies.werewolf
package choice
package disjunction

sealed abstract class Disjunction {

  final def fanIn[R, F <: FanInPoly[R]](poly: F)(using f: CanFanInDisjunction[R, this.type, F]): R =
    f(this)

}

// There are no instances of this class
sealed abstract class CNothing extends Disjunction {
  def absurd: Nothing
}

sealed case class <+>[A, B <: Disjunction](
  val impl: Either[A, B],
) extends Disjunction {
  def toEither: Either[A, B] = impl
}
