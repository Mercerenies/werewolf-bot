
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, NamedEntityMatcher}
import util.{Numeral, Grammar}

class FiniteChoice[+A <: NamedEntity](
  private val entities: List[A],
  private val expected: Int = 1,
  private val allowRepeats: Boolean = false,
) extends Choice[List[A]] {

  private val matcher: NamedEntityMatcher[A] =
    NamedEntity.matcher(entities)

  // Postcondition: If the return value is a Right(...), then the
  // length of the returned list is equal to this.expected.
  override def parse(text: String): Either[ChoiceError, List[A]] = {
    val matches = matcher.findAll(text).toList
    if (matches.length == expected) {
      if ((allowRepeats) || (util.findDuplicate(matches).isEmpty)) {
        Right(matches)
      } else {
        Left(ChoiceError.RepeatedElement)
      }
    } else if (matches == Nil) {
      Left(ChoiceError.NoFurtherOptions)
    } else {
      Left(ChoiceError.WrongNumber(expected, matches.length))
    }
  }

  override def blurb: String =
    // Special case if we're expecting only a single unique value
    if ((entities.length == 1) && (expected == 1)) {
      s"the value ${entities(0).name}"
    } else {
      Numeral.fromInt(expected) + " of " + Grammar.conjunctionList(entities.map(_.name), "or")
    }

}

object FiniteChoice {

  // Convenience wrappers which return tuples, for common use cases
  // with small numbers of inputs. All of these assume repeats are
  // disallowed.

  class OneOf[+A <: NamedEntity](
    entities: List[A],
  ) extends Choice[A] {
    private val impl = FiniteChoice(entities, 1, false)

    export impl.blurb

    override def parse(text: String): Either[ChoiceError, A] =
      impl.parse(text) map {
        case List(x) => x
        case xs => throw new AssertionError(s"Bad result from impl.parse: ${xs}")
      }

  }

  class TwoOf[+A <: NamedEntity](
    entities: List[A],
  ) extends Choice[(A, A)] {
    private val impl = FiniteChoice(entities, 2, false)

    export impl.blurb

    override def parse(text: String): Either[ChoiceError, (A, A)] =
      impl.parse(text) map {
        case List(x, y) => (x, y)
        case xs => throw new AssertionError(s"Bad result from impl.parse: ${xs}")
      }

  }

  class OneOrTwoOf[+A <: NamedEntity](
    entities: List[A],
  ) extends Choice[(A, Option[A])] {
    import syntax.:+:

    private val one = FiniteChoice(entities, 1, false)
    private val two = FiniteChoice(entities, 2, false)

    override def blurb: String =
      "one or two of " + Grammar.conjunctionList(entities.map(_.name), "or")

    override def parse(text: String): Either[ChoiceError, (A, Option[A])] =
      (two :+: one).parse(text) map {
        case Left(List(x, y)) => (x, Some(y))
        case Right(List(x)) => (x, None)
        case x => throw new AssertionError(s"Bad result from impl.parse: ${x}")
      }

  }

}
