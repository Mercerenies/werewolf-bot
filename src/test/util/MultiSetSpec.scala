
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

  it should "produce its values in naturally sorted order" in {
    val s = MultiSet.from(List(1, 3, 2, 1, 2))
    s.sorted should be (List(1, 1, 2, 2, 3))
  }

  it should "produce its values in sorted order using a key" in {
    val s = MultiSet.from(List(1, 3, 2, 1, 2))
    s.sortBy { - _ } should be (List(3, 2, 2, 1, 1))
  }

  it should "produce its values in sorted order using a comparator function" in {
    val s = MultiSet.from(List(1, 3, 2, 1, 2))
    s.sortWith { _ > _ } should be (List(3, 2, 2, 1, 1))
  }

  it should "allow adding elements through the + method" in {
    val s = MultiSet(1, 2, 3)
    s + 1 should be (MultiSet(1, 2, 3, 1))
    s + 1 + 10 should be (MultiSet(1, 2, 3, 1, 10))
  }

  it should "allow removing elements through the - method" in {
    val s = MultiSet(1, 1, 1, 2, 3)
    s - 1 should be (MultiSet(1, 2, 3, 1))
    s - 1 - 1 should be (MultiSet(1, 2, 3))
    s - 1 - 1 - 1 should be (MultiSet(2, 3))
  }

  it should "change nothing if an attempt is made to remove a nonexistent element" in {
    val s = MultiSet("a", "b", "c")
    s - "someRandomElement" should be (s)
  }

  it should "allow containment querying with the 'contains' method" in {
    val s = MultiSet(1, 1, 1, 2, 3)
    s.contains(1) should be (true)
    s.contains(2) should be (true)
    s.contains(3) should be (true)
    s.contains(4) should be (false)
    (s - 1).contains(1) should be (true)
    (s - 2).contains(2) should be (false)
  }

}
