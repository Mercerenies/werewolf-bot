
package com.mercerenies.werewolf
package game
package parser

import util.TextDecorator
import name.NamedEntity

import scalaz.*
import Scalaz.*

import scala.util.matching.Regex

// Parses a long code block (with triple backticks) full of names
// coming from a list of NamedEntity objects.
class ListParser[+A <: NamedEntity](
  private val knownEntities: List[A],
) {

/* /////
  def parse(text: String): String \/ List[A] = {
    
  }
 */

}

object ListParser {

  private val blockRegex: Regex =
    raw"${Regex.quote(TextDecorator.longCode.prefix)}(.*?)${Regex.quote(TextDecorator.longCode.suffix)}".r

  def findLongCodeBlocks(text: String): Option[List[String]] = {
    val matches = blockRegex.findAllMatchIn(text)
    if (matches.isEmpty) {
      // No matches, so we failed to find anything.
      None
    } else {
      Some(matches.map(_.group(1)))
    }
  }

}
