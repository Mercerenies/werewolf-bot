
package com.mercerenies.werewolf
package game
package parser

import util.TextDecorator
import name.NamedEntity

import scalaz.*
import Scalaz.*

import scala.util.matching.Regex

// General-purpose parsing helpers
object Parsing {

  private val blockRegex: Regex =
    raw"(?s)${Regex.quote(TextDecorator.longCode.prefix)}(.*?)${Regex.quote(TextDecorator.longCode.suffix)}".r

  private val boldRegex: Regex =
    raw"(?s)${Regex.quote(TextDecorator.bold.prefix)}(.*?)${Regex.quote(TextDecorator.bold.suffix)}".r

  def findLongCodeBlocks(text: String): List[String] = {
    val matches = blockRegex.findAllMatchIn(text)
    matches.map(_.group(1)).toList
  }

  def findBoldText(text: String): List[String] = {
    val matches = boldRegex.findAllMatchIn(text)
    matches.map(_.group(1)).toList
  }

}
