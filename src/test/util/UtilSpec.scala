
package com.mercerenies.werewolf
package util

class UtilSpec extends UnitSpec {

  // TODO The rest of this file (only tr is tested)

  "The util.tr function" should "create a mapping from a pair of strings" in {
    tr("", "") should be (Map.empty)
    tr("a", "1") should be (Map('a' -> '1'))
    tr("abc", "123") should be (Map('a' -> '1', 'b' -> '2', 'c' -> '3'))
  }

  it should "take the later key in case of duplicates" in {
    tr("abca", "1234") should be (Map('a' -> '4', 'b' -> '2', 'c' -> '3'))
  }

  it should "throw an exception if given strings of non-equal length" in {
    an [IllegalArgumentException] should be thrownBy tr("abca", "d")
    an [IllegalArgumentException] should be thrownBy tr("ab", "abcde")
  }

}
