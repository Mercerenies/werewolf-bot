
package com.mercerenies.werewolf
package game
package votes

import scala.math.Ordering.Implicits.*

class DeathStatusSpec extends UnitSpec {

  "DeathStatus" should "provide a valid ordering" in {
    (DeathStatus.Alive < DeathStatus.Dead) should be (true)
    (DeathStatus.Dead < DeathStatus.Protected) should be (true)
    (DeathStatus.Alive < DeathStatus.Protected) should be (true)
  }

}
