
package com.mercerenies.werewolf
package choice
package disjunction

sealed trait CanFanInDisjunction[R, D <: Disjunction, F <: FanInPoly[R]] {

  def apply(disj: D): R

}

object CanFanInDisjunction {

  given CanFanInOnNothing[R, F <: FanInPoly[R]]: CanFanInDisjunction[R, CNothing, F] with
    def apply(disj: CNothing) = disj.absurd

  given CanFanInOnPlus[R, A, B <: Disjunction, F <: FanInPoly[R]](
    using first: FanInPoly.Case[A, R], second: CanFanInDisjunction[R, B, F],
  ): CanFanInDisjunction[R, A <+> B, F] with
    def apply(disj: A <+> B) = disj.toEither match {
      case Left(a) => first(a)
      case Right(b) => second(b)
    }

}
