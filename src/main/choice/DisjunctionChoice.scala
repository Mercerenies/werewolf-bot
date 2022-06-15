
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, NamedEntityMatcher}

class DisjunctionChoice[+A, +B](
  private val first: Choice[A],
  private val second: Choice[B],
) extends Choice[Either[A, B]] {

  override def parse(text: String): Either[ChoiceError, Either[A, B]] = {
    first.parse(text).map { Left(_) } orElse
      second.parse(text).map { Right(_) }
  }

}
