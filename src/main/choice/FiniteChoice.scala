
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, NamedEntityMatcher}

class FiniteChoice[+A <: NamedEntity](
  private val entities: List[A],
  private val count: Int = 1,
  private val allowRepeats: Boolean = false,
) extends Choice[A] {

  private val matcher: NamedEntityMatcher[A] =
    NamedEntity.matcher(entities)

  override def parse(text: String): Either[ChoiceError, A] =
    matcher.findUnique(text) match {
      case None => Left(ChoiceError("Invalid choice"))
      case Some(x) => Right(x)
    }

}
