
package com.mercerenies.werewolf
package choice

// Always succeeds, returns a default value.
class ConstantChoice[A](private val value: A) extends Choice[A] {

  override def parse(text: String): Either[ChoiceError, A] =
    Right(value)

}

object ConstantChoice {

  val unit: ConstantChoice[Unit] = ConstantChoice(())

  val emptyTuple: ConstantChoice[EmptyTuple.type] = ConstantChoice(EmptyTuple)

}
