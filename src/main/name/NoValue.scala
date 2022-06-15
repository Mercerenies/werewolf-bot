
package com.mercerenies.werewolf
package name

import util.RegexUtil

import scala.util.matching.Regex

// General purpose "no value" object for use as a NamedEntity.
sealed trait NoValue extends NamedEntity {
  val name: String = "None"
  val aliases: List[String] = Nil
}

object NoValue extends NoValue
