
package com.mercerenies.werewolf
package util

object Numeral {

  def fromInt(n: Int): String =
    n match {
      case 0 => "zero"
      case 1 => "one"
      case 2 => "two"
      case 3 => "three"
      case 4 => "four"
      case 5 => "five"
      case 6 => "six"
      case 7 => "seven"
      case 8 => "eight"
      case 9 => "nine"
      case _ => n.toString
    }

  def letter(n: Int): Char =
    if ((n < 0) || (n > 25)) {
      '?'
    } else {
      ('a' + n).asInstanceOf[Char]
    }

}
