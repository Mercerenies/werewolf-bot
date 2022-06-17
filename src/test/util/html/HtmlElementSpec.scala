
package com.mercerenies.werewolf
package util
package html

class HtmlElementSpec extends UnitSpec {

  trait FakeFunction {
    def apply(x: HtmlFragment): Unit
  }

  "The HtmlElement class" should "call its body function once with the given fragment object" in {
    val mockFragment: HtmlFragment = mock
    val mockFunction: FakeFunction = mock

    given HtmlFragment = mockFragment

    val elt = HtmlElement("abc")
    elt {
      mockFunction(summon[HtmlFragment])
    }

    verify(mockFunction).apply(mockFragment)

  }

  it should "prepend the contents with the opening tag and postfix with the closing tag" in {
    val builder = StringBuilderHtmlFragment()
    given HtmlFragment = builder
    val elt = HtmlElement("abc")
    elt {
      summon[HtmlFragment].append("foobar")
    }
    builder.mkString should be ("<abc>foobar</abc>")
  }

  it should "apply the opening and closing tags even in the case of empty contents" in {
    val builder = StringBuilderHtmlFragment()
    given HtmlFragment = builder
    val elt = HtmlElement("abc")
    elt { }
    builder.mkString should be ("<abc></abc>")
  }

  it should "err if constructed with a bad tag name" in {
    an [IllegalArgumentException] should be thrownBy HtmlElement("9")
    an [IllegalArgumentException] should be thrownBy HtmlElement("")
    an [IllegalArgumentException] should be thrownBy HtmlElement("<>")
    an [IllegalArgumentException] should be thrownBy HtmlElement("a b")
    an [IllegalArgumentException] should be thrownBy HtmlElement("-")
    an [IllegalArgumentException] should be thrownBy HtmlElement("\"")
  }

}
