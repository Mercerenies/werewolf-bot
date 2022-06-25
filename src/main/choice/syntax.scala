
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, NoValue}

import scala.annotation.targetName
import scala.reflect.ClassTag

object syntax {

  val noValue: Choice[NoValue] =
    FiniteChoice.OneOf(List(NoValue))

  def oneOf[A <: NamedEntity](options: List[A]): Choice[A] =
    FiniteChoice.OneOf(options)

  def twoOf[A <: NamedEntity](options: List[A]): Choice[(A, A)] =
    FiniteChoice.TwoOf(options)

  extension[A](self: Choice[A])

    @targetName("orElse")
    def :+:[B](other: Choice[B]): Choice[Either[A, B]] =
      DisjunctionChoice(self, other)

    // TODO Try to get overload resolution working between this and
    // :*: (andThenRec) below.
    @targetName("andThen")
    def :**:[B](other: Choice[B]): Choice[(A, B)] =
      ConjunctionChoice(self, ConjunctionChoice(other, ConjunctionChoice.unit))

    def formattedList: Choice[A] =
      FormattedListChoice(self)

    @targetName("andThenRec")
    def :*:[B <: Tuple](other: Choice[B])(using ClassTag[B]): Choice[A *: B] =
      ConjunctionChoice(self, other)

}
