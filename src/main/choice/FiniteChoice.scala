
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, NamedEntityMatcher}

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
        Left(ChoiceError("No duplicates allowed"))
      }
    } else {
      Left(ChoiceError("Invalid choice"))
    }
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

    override def parse(text: String): Either[ChoiceError, A] =
      impl.parse(text) map {
        case List(x) => x
        case xs => throw new AssertionError(s"Bad result from impl.parse: ${xs}")
      }

  }

  class TwoOf[+A <: NamedEntity](
    entities: List[A],
  ) extends Choice[(A, A)] {
    private val impl = FiniteChoice(entities, 1, false)

    override def parse(text: String): Either[ChoiceError, (A, A)] =
      impl.parse(text) map {
        case List(x, y) => (x, y)
        case xs => throw new AssertionError(s"Bad result from impl.parse: ${xs}")
      }

  }

}
