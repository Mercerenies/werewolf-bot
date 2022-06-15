
package com.mercerenies.werewolf
package choice

// I might make this opaque later...
type ChoiceError = String

object ChoiceError {

  def apply(x: String): ChoiceError = x

}
