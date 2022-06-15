
package com.mercerenies.werewolf
package util

import scala.annotation.{targetName, tailrec}
import scala.util.matching.Regex

// Regex helper functions.
object RegexUtil {

  final class DispatchTree(
    val isLeaf: Boolean,
    val mapping: Map[Char, DispatchTree],
  ) {

    // TODO Word boundaries on options that start / end with word chars?
    def toRegex(caseSensitive: Boolean = true): Regex = {
      var re = toRegexString
      if (!caseSensitive) {
        re = s"(?i)${re}"
      }
      re.r
    }

    private def toRegexString: String =
      // Deal with a few common cases that can be simpler
      if (mapping.isEmpty) {
        ""
      } else {
        val options = mapping.map((ch, tree) => s"${quote(ch)}${tree.toRegexString}")
        val alts = options.mkString("|")
        val baseString = if ((mapping.size == 1) && !isLeaf) { alts } else { s"(?:${alts})" }
        if (isLeaf) {
          s"${baseString}?"
        } else {
          baseString
        }
      }

  }

  object DispatchTree {
    val zero = DispatchTree(false, Map())
    val one = DispatchTree(true, Map())
  }

  val quoteWhitelist = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890 _".toSet

  def uncons[A](list: List[A]): Option[(A, List[A])] =
    list match {
      case Nil => None
      case x :: xs => Some((x, xs))
    }

  @targetName("buildTree")
  def buildTree(options: List[String]): DispatchTree =
    buildTree(options.map { _.toList })

  @targetName("buildTreeFromList")
  def buildTree(options: List[List[Char]]): DispatchTree =
    if (options.isEmpty) {
      DispatchTree.zero
    } else {
      val isLeaf = options.exists(_.isEmpty)
      val mapping = options.flatMap { uncons(_) }.groupMap { (x, _) => x } { (_, xs) => xs }
      DispatchTree(
        isLeaf,
        mapping map { case (k, rest) => (k, buildTree(rest)) },
      )
    }

  // Given a list of options, build an efficient regex to parse
  // exactly those options.
  def build(options: IterableOnce[String], caseSensitive: Boolean = true): Regex =
    val tree = buildTree(options.iterator.toList)
    tree.toRegex(caseSensitive)

  // I don't like it when my regex looks like
  // \QH\E\Qe\E\Ql\E\Ql\E\Qo\E, so letters and numbers don't need to
  // be escaped, as far as I'm concerned.
  def quote(char: Char): String =
    if (quoteWhitelist.contains(char)) {
      char.toString
    } else {
      Regex.quote(char.toString)
    }

}
