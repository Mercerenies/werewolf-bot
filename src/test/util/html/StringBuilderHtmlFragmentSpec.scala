
package com.mercerenies.werewolf
package util
package html

class StringBuilderHtmlFragmentSpec extends UnitSpec {

  "The StringBuilderHtmlFragment class" should "act as a StringBuilder and produce a string" in {
    val builder = StringBuilderHtmlFragment()
    builder.append("ABC")
    builder.append("100")
    builder.append("\n")
    builder.append("200")
    builder.mkString should be ("ABC100\n200")
  }

  it should "produce results which do not interfere with each other if mkString is called multiple times" in {
    val builder = StringBuilderHtmlFragment()

    builder.append("foo")
    val x = builder.mkString

    builder.append("bar")
    val y = builder.mkString

    x should be ("foo")
    y should be ("foobar")
  }

}
