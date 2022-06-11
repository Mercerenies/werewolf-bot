
package com.mercerenies.werewolf
package game

class RulesSpec extends UnitSpec {

  "The Rules singleton" should "return an appropriate count of players needed" in {

    Rules.rolesNeeded(5) should be (8)

    Rules.rolesNeeded(7) should be (10)

  }

}
