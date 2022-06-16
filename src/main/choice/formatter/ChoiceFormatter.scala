
package com.mercerenies.werewolf
package choice
package formatter

import util.Grammar

// Choice[A] is frequently used with A as a nested collection of
// tuple, list, or either. We want to get through that arbitrary
// nesting and print out the values we actually care about.
// ChoiceFormatter reaches through the nesting and prints out the
// leaves in a nice, user-friendly way.
trait ChoiceFormatter[A] {
  def format(value: A): String
}

object ChoiceFormatter {

  sealed trait TupleChoiceFormatter[A] extends ChoiceFormatter[A] {

    final def format(value: A): String =
      Grammar.conjunctionList(toEntriesList(value))

    // Specific to tuples: Convert this value to a list of strings using
    // the appropriate formatter. This will then be postprocessed by
    // .format. This function is the recursive part that runs on Scala
    // 3's HList-like tuples. .format merely delegates to this.
    def toEntriesList(value: A): List[String]

  }

  def apply[A](using inst: ChoiceFormatter[A]): ChoiceFormatter[A] =
    inst

  def format[A](value: A)(using inst: ChoiceFormatter[A]): String =
    inst.format(value)

  given NothingChoiceFormatter : ChoiceFormatter[Nothing] with

    def format(value: Nothing): String =
      value // Absurd :)

  given EitherChoiceFormatter[A, B](
    using lhs: ChoiceFormatter[A], rhs: ChoiceFormatter[B],
  ) : ChoiceFormatter[Either[A, B]] with

    def format(value: Either[A, B]): String =
      value.fold(lhs.format, rhs.format)

  given ListChoiceFormatter[A](
    using inner: ChoiceFormatter[A],
  ) : ChoiceFormatter[List[A]] with

    def format(value: List[A]): String =
      Grammar.conjunctionList(value.map(inner.format))

  given EmptyTupleChoiceFormatter : TupleChoiceFormatter[EmptyTuple] with

    def toEntriesList(value: EmptyTuple): List[String] =
      Nil

  given NonEmptyTupleChoiceFormatter[A, B <: Tuple](
    using lhs: ChoiceFormatter[A], rhs: TupleChoiceFormatter[B],
  ) : TupleChoiceFormatter[A *: B] with

    def toEntriesList(value: A *: B): List[String] =
      value match {
        case (head *: tail) => {
          lhs.format(head) :: rhs.toEntriesList(tail)
        }
      }

}
