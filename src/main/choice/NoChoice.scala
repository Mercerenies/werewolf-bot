
package com.mercerenies.werewolf
package choice

object NoChoice extends Choice[Nothing] {

  override def parse(text: String): Either[ChoiceError, Nothing] =
    Left(ChoiceError.NoFurtherOptions)

}
