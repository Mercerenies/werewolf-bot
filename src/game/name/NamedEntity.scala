
package com.mercerenies.werewolf
package game
package name

trait NamedEntity {
  def name: String
  def aliases: List[String]

  final def allNames: List[String] =
    name :: aliases

  final def matches(name: String): Boolean =
    allNames.map(_.toLowerCase).contains(name.toLowerCase)

}
