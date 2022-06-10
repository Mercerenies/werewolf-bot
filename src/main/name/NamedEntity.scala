
package com.mercerenies.werewolf
package name

import util.RegexUtil

import scala.util.matching.Regex

trait NamedEntity {
  def name: String
  def aliases: List[String]

  final def allNames: List[String] =
    name :: aliases

  final def matches(name: String): Boolean =
    allNames.map(_.toLowerCase).contains(name.toLowerCase)

  override def toString: String =
    name

}

object NamedEntity {

  def compileRegex(entities: Iterable[NamedEntity]): Regex =
    RegexUtil.build(entities.flatMap(_.allNames), caseSensitive = false)

  def matcher[A <: NamedEntity](entities: Iterable[A]): NamedEntityMatcher[A] =
    NamedEntityMatcher(entities.toList)

  def findMatch[A <: NamedEntity](text: String, entities: Iterable[A]): Option[A] =
    entities.find(_.matches(text))

}
