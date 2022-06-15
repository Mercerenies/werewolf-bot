
package com.mercerenies.werewolf
package choice

trait Choice[+A] {

  def parse(text: String): Either[ChoiceError, A]

}
