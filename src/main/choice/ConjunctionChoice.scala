
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, NamedEntityMatcher}

class ConjunctionChoice[+A, +B <: Tuple](
  val first: Choice[A],
  val second: Choice[B],
) extends Choice[A *: B] {

  override def parse(text: String): Either[ChoiceError, A *: B] = {
    for {
      a <- first.parse(text)
      b <- second.parse(text)
    } yield {
      a *: b
    }
  }

  override val blurb: String = s"${first.blurb} and ${second.blurb}"

}

object ConjunctionChoice {

  val unit: Choice[EmptyTuple] =
    ConstantChoice.emptyTuple

}
