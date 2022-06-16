
package com.mercerenies.werewolf
package game
package night

import util.Grammar
import response.MessageResponse
import choice.formatter.ChoiceFormatter

// Note: This is really a legacy object from when .format() took Any
// and wasn't typesafe at all. It just delegates to the
// ChoiceFormatter, which does implicit resolution.
object SelectionConfirmFormatter {

  // Tries to intelligently produce a list of the options chosen. This
  // function intelligently handles Either, List, and tuples of any
  // size, and falls back to toString on all other objects.
  def format[A](input: A)(using ChoiceFormatter[A]): String =
    ChoiceFormatter.format(input)

}
