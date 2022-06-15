
package com.mercerenies.werewolf
package name

import scala.util.matching.Regex

final class NamedEntityMatcher[A <: NamedEntity](
  val entities: List[A],
) {

  private val regex: Regex = NamedEntity.compileRegex(entities)

  def findAll(text: String): Iterator[A] =
    regex.findAllMatchIn(text).flatMap { m => NamedEntity.findMatch(m.matched, entities) }

  def findUnique(text: String): Option[A] =
    // Only return Some if there's a single unique match
    findAll(text).toList match {
      case List(x) => Some(x)
      case _ => None
    }

}
