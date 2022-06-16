
package com.mercerenies.werewolf
package game
package night

import util.Grammar
import response.MessageResponse

object SelectionConfirmFormatter {

  // Tries to intelligently produce a list of the options chosen. This
  // function intelligently handles Either, List, and tuples of any
  // size, and falls back to toString on all other objects.
  def format(input: Any): String =
    input match {
      case input: Either[?, ?] => {
        // If it's an Either, then just call format on whatever is
        // inside it.
        input.fold(format, format)
      }
      case input: List[?] => {
        // If it's a list, then use Grammar.conjunctionList to
        // construct the result.
        Grammar.conjunctionList(input.map(_.toString))
      }
      case input: Tuple => {
        // If it's a tuple, make it a list.
        format(input.toList)
      }
      case input => {
        // Otherwise, toString
        input.toString
      }
    }

}
