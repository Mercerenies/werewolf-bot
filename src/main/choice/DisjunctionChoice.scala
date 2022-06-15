
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, NamedEntityMatcher}

class DisjunctionChoice[+A, +B](
  private val first: Choice[A],
  private val second: Choice[B],
) extends Choice[Either[A, B]] {

  override def parse(text: String): Either[ChoiceError, Either[A, B]] = {
    val a = first.parse(text).map { Left(_) }
    val b = second.parse(text).map { Right(_) }
    (a, b) match {
      case (Right(a), _) => Right(a)
      case (_, Right(b)) => Right(b)
      case (Left(e1), Left(e2)) => Left(e1 betterError e2)
    }
  }

}
