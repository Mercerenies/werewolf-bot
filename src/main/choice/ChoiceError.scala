
package com.mercerenies.werewolf
package choice

enum ChoiceError {
  case NoFurtherOptions
  case WrongNumber(expected: Int, actual: Int)
  case RepeatedElement

  infix def betterError(that: ChoiceError): ChoiceError =
    if (this == that) {
      // No conflict.
      this
    } else if ((this == RepeatedElement) || (that == RepeatedElement)) {
      // Always show RepeatedElement if there is one.
      RepeatedElement
    } else {
      // Two different WrongNumber are incompatible, so just show
      // NoFurtherOptions (the most generic message).
      NoFurtherOptions
    }

}
