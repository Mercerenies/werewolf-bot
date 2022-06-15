
package com.mercerenies.werewolf
package choice
package disjunction

trait FanInPoly[R] {
  import FanInPoly.Case

  final def fn[A](f: (A) => R): Case[A, R] =
    new Case { export f.apply }

  final def apply[A](input: A)(using f: Case[A, R]): R =
    f(input)

}

object FanInPoly {

  trait Case[-A, +R] {
    def apply(input: A): R
  }

}
