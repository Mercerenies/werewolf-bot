
package com.mercerenies.werewolf
package name

trait NamedEntity {
  def name: String
  def aliases: List[String]

  final def allNames: List[String] =
    name :: aliases

  final def matches(name: String): Boolean =
    allNames.map(_.toLowerCase).contains(name.toLowerCase)

}

object NamedEntity {

  def findMatch[A <: NamedEntity](text: String, entities: Iterable[A]): Option[A] =
    entities.find(_.matches(text))

}
