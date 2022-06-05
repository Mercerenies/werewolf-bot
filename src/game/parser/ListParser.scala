
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
class ListParser[A <: NamedEntity](
  private val knownEntities: List[A],
  private val noun: String,
) {

  private def findLongCodeBlocks(text: String): String \/ List[String] =
    \/.fromOption(s"Please ping me with a code block containing each ${noun} on a separate line.") {
      ListParser.findLongCodeBlocks(text)
    }

  private def findMatch(text: String): String \/ A =
    \/.fromOption(s"I don't recognize a ${noun} called '${text}'.") {
      NamedEntity.findMatch(text, knownEntities)
    }

  def parse(text: String): String \/ List[A] =
    for {
      blocks <- findLongCodeBlocks(text)
      lines = blocks.flatMap { _.split("\n").filter(_ != "").toList }
      entities <- lines.traverse { findMatch(_) }
    } yield {
      entities
    }

}

object ListParser {

  private val blockRegex: Regex =
    raw"(?s)${Regex.quote(TextDecorator.longCode.prefix)}(.*?)${Regex.quote(TextDecorator.longCode.suffix)}".r

  def findLongCodeBlocks(text: String): Option[List[String]] = {
    val matches = blockRegex.findAllMatchIn(text)
    if (matches.isEmpty) {
      // No matches, so we failed to find anything.
      None
    } else {
      Some(matches.map(_.group(1)).toList)
    }
  }

}
