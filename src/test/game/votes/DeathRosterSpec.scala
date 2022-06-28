
package com.mercerenies.werewolf
package game
package votes

class DeathRosterSpec extends UnitSpec {

  "The DeathRoster class" should "be constructible from an iterable of entries" in {
    val example = DeathRoster.from(List(1, 2, 3, 4))
    example.deaths should be (Map(1 -> DeathStatus.Alive, 2 -> DeathStatus.Alive, 3 -> DeathStatus.Alive, 4 -> DeathStatus.Alive))
  }

  it should "be constructible from a Votals object" in {
    val votals = Votals(Map(1 -> 1, 2 -> 3, 3 -> 3))
    val example = DeathRoster.from(votals)
    example.deaths should be (Map(1 -> DeathStatus.Alive, 2 -> DeathStatus.Alive, 3 -> DeathStatus.Alive))
  }

  it should "update an entry with the maximum of the current value and the new value" in {
    var example = DeathRoster.from(List(1, 2, 3, 4))
    example(1) should be (DeathStatus.Alive)
    example = example.updated(1, DeathStatus.Protected)
    example(1) should be (DeathStatus.Protected)
    example = example.updated(1, DeathStatus.Dead)
    example(1) should be (DeathStatus.Protected) // Doesn't change
  }

  it should "add entries if they don't exist" in {
    var example = DeathRoster.from(List(1, 2))
    example.get(4) should be (None)
    example = example.updated(4, DeathStatus.Alive)
    example.get(4) should be (Some(DeathStatus.Alive))
    example.get(5) should be (None)
    example = example.updated(5, DeathStatus.Dead)
    example.get(5) should be (Some(DeathStatus.Dead))
  }

}
