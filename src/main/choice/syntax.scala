
package com.mercerenies.werewolf
package choice

import name.NamedEntity

object syntax {

  def oneOf[A <: NamedEntity](options: List[A]): Choice[A] =
    FiniteChoice.OneOf(options)

  def twoOf[A <: NamedEntity](options: List[A]): Choice[(A, A)] =
    FiniteChoice.TwoOf(options)

  extension[A](self: Choice[A])

    def :+:[B](other: Choice[B]): Choice[Either[A, B]] =
      DisjunctionChoice(self, other)

}
