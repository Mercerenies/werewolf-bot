
package com.mercerenies.werewolf
package choice

import util.Numeral

// FormattedListChoice(x) behaves identically (as a parser) to x but
// it has a more intelligent blurb. Specifically, FormattedListChoice
// is intended to be used with a DisjunctionChoice argument (and
// possibly further DisjunctionChoices below that) and creates a
// neatly formatted, lettered list of the options.
class FormattedListChoice[+A](
  private val impl: Choice[A],
) extends Choice[A] {

  export impl.parse

  override def blurb: String = {
    val options = FormattedListChoice.flattenDisjunctions(impl)
    val optionsText = options.zipWithIndex.map { (opt, index) =>
      "(" + Numeral.letter(index) + ") " + opt.blurb.capitalize
    }.mkString("\n")
    s"one of the following\n${optionsText}"
  }

}

object FormattedListChoice {

  // Given a tree of DisjunctionChoices, flatten it into an
  // inorder-traversed list of the non-disjunction leaf nodes.
  def flattenDisjunctions(choice: Choice[?]): List[Choice[?]] =
    choice match {
      case x: DisjunctionChoice[?, ?] => flattenDisjunctions(x.first) ++ flattenDisjunctions(x.second)
      case x => List(x)
    }

}
