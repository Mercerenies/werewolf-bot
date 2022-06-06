
package com.mercerenies.werewolf
package name

import scala.util.matching.Regex

trait NamedEntity {
  def name: String
  def aliases: List[String]

  final def allNames: List[String] =
    name :: aliases

  final def matches(name: String): Boolean =
    allNames.map(_.toLowerCase).contains(name.toLowerCase)

}

object NamedEntity {

  def compileRegex(entities: Iterable[NamedEntity]): Regex = {
    val body = entities.flatMap(_.allNames).map(Regex.quote).mkString("|")
    s"(?is)${body}".r
  }

  def findMatch[A <: NamedEntity](text: String, entities: Iterable[A]): Option[A] =
    entities.find(_.matches(text))

  def findAll[A <: NamedEntity](text: String, entities: Iterable[A]): Iterator[A] =
    compileRegex(entities).findAllMatchIn(text).flatMap { m => findMatch(m.matched, entities) }

}
