
package com.mercerenies.werewolf
package util

class MultiSetSpec extends UnitSpec {

  private object NotAMultiSet

  "The MultiSet class" should "compare equal to equivalent collections" in {
    (MultiSet(1, 2, 3) == MultiSet(3, 2, 1)) should be (true)
    (MultiSet(1, 1, 2, 3) == MultiSet(3, 1, 1, 2)) should be (true)
    (MultiSet(1, 1, 2, 3) == MultiSet(3, 1, 2)) should be (false)
    (MultiSet(1, 1, 2, 3) == NotAMultiSet) should be (false)
  }

  it should "convert to a list with the appropriate elements" in {
    val set = MultiSet(1, 1, 2, 3)
    val list = set.toList
    // Order doesn't matter, but values do
    list.contains(1) should be (true)
    list.contains(2) should be (true)
    list.contains(3) should be (true)
    list.filter { _ == 1 } should be (List(1, 1))
    list.length should be (4)
  }

  it should "be constructible from other iterable collections" in {
    MultiSet.from(List(1, 1, 3, 2)) should be (MultiSet(3, 2, 1, 1))
  }

  /////

}
