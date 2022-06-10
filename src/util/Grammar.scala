
package com.mercerenies.werewolf
package util

object Grammar {

  def pluralize(number: Long, text: String): String =
    if (number == 1) {
      s"${number} ${text}"
    } else {
      s"${number} ${text}s"
    }

  // Comma-separated list ending with a conjunction between the last
  // two items.
  def conjunctionList(items: List[String], conjunction: String = "and"): String =
    items match {
      case Nil => ""
      case (x :: Nil) => x
      case (x :: y :: Nil) => s"${x} ${conjunction} ${y}"
      case (x :: xs) => s"${x}${conjunctionListRec(xs, conjunction)}"
    }

  private def conjunctionListRec(items: List[String], conjunction: String): String =
    items match {
      case Nil => "" // Should not happen since conjunctionList won't call this function in that case.
      case (x :: Nil) => s", ${conjunction} ${x}"
      case (x :: xs) => s", ${x}${conjunctionListRec(xs, conjunction)}"
    }

}
