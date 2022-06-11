
package com.mercerenies.werewolf
package peano

class NumSpec extends UnitSpec {

  import Num.{Zero, Succ}

  "Num" should "convert to an integer" in {
    Zero.toInt should be (0)
    Succ(Succ(Zero)).toInt should be (2)
  }

}
