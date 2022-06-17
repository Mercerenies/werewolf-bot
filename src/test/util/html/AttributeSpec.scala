
package com.mercerenies.werewolf
package util
package html

class AttributeSpec extends UnitSpec {

  "The Attribute class" should "produce an attribute with the correct text" in {
    Attribute("abc", "def").mkString should be ("abc=\"def\"")
  }

  it should "work in case of an empty value" in {
    Attribute("abc", "").mkString should be ("abc=\"\"")
  }

  it should "produce escaped text in the value" in {
    Attribute("abc", "&\"").mkString should be ("abc=\"&amp;&quot;\"")
  }

  it should "fail on invalid attribute names" in {
    an [IllegalArgumentException] should be thrownBy Attribute("9", "")
    an [IllegalArgumentException] should be thrownBy Attribute("", "")
    an [IllegalArgumentException] should be thrownBy Attribute("<>", "")
    an [IllegalArgumentException] should be thrownBy Attribute("a b", "")
    an [IllegalArgumentException] should be thrownBy Attribute(" ", "")
    an [IllegalArgumentException] should be thrownBy Attribute("-", "")
    an [IllegalArgumentException] should be thrownBy Attribute("\"", "")
  }

}
