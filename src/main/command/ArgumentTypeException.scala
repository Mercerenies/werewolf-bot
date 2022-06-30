
package com.mercerenies.werewolf
package command

class ArgumentTypeException(
  val expected: ArgumentType[?],
  message: String,
) extends Exception(message) {

  def this(expected: ArgumentType[?]) =
    this(expected, "Bad argument to command")

}
