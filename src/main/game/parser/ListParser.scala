
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
    Parsing.findLongCodeBlocks(text) match {
      case Nil => s"Please ping me with a code block containing each ${noun} on a separate line.".left
      case xs => xs.right
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
