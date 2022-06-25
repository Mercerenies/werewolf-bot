
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, NamedEntityMatcher}

import scala.reflect.ClassTag

class ConjunctionChoice[+A, +B <: Tuple](
  val first: Choice[A],
  val second: Choice[B],
)(
  using cls: ClassTag[B], // Used for blurb
) extends Choice[A *: B] {

  override def parse(text: String): Either[ChoiceError, A *: B] = {
    for {
      a <- first.parse(text)
      b <- second.parse(text)
    } yield {
      a *: b
    }
  }

  override val blurb: String =
    // TODO (HACK) Overhaul the blurb system to be more like
    // ChoiceFormatter (use implicit resolution rather than Java's
    // reflection, so we can actually make sure it all typechecks)
    if (summon[ClassTag[EmptyTuple]].runtimeClass.isAssignableFrom(cls.runtimeClass)) {
      first.blurb
    } else {
      s"${first.blurb} and ${second.blurb}"
    }

}

object ConjunctionChoice {

  val unit: Choice[EmptyTuple] =
    ConstantChoice.emptyTuple

}
