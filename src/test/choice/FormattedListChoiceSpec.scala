
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, SimpleName, NoValue}
import ChoiceError.*
import syntax.*

class FormattedListChoiceSpec extends UnitSpec {

  val foo = SimpleName("Foo")
  val bar = SimpleName("Bar")
  val baz = SimpleName("Baz")

  val options = List(foo, bar, baz)

  val number1 = SimpleName("1")
  val number2 = SimpleName("2")
  val number3 = SimpleName("3")

  val numOptions = List(number1, number2, number3)

  "FormattedListChoice" should "parse just like the underlying parser" in {
    val parser = (syntax.oneOf(options) :+: syntax.twoOf(numOptions)).formattedList
    parser.parse("e") should be (Left(NoFurtherOptions))
    parser.parse("foo") should be (Right( Left(foo) ))
    parser.parse("aaa bar bbb") should be (Right( Left(bar) ))
    parser.parse("aaa bar bar bbb") should be (Left(NoFurtherOptions))
    parser.parse("aaa 1 2 bbb") should be (Right( Right((number1, number2)) ))
    parser.parse("aaa 1 1 bbb") should be (Left(RepeatedElement))
    parser.parse("aaa bar 1 bbb") should be (Right( Left(bar) ))
    parser.parse("aaa bar baz 1 2 bbb") should be (Right( Right((number1, number2)) ))
  }

  it should "provide a list format for the blurb" in {
    val parser = (syntax.oneOf(options) :+: syntax.twoOf(numOptions)).formattedList
    parser.blurb should be ("one of the following\n(a) One of Foo, Bar, or Baz\n(b) Two of 1, 2, or 3")
  }

  it should "provide a list format for the blurb in the case of three inputs" in {
    val parser = (syntax.oneOf(options) :+: syntax.twoOf(numOptions) :+: syntax.oneOf(options)).formattedList
    parser.blurb should be ("one of the following\n(a) One of Foo, Bar, or Baz\n(b) Two of 1, 2, or 3\n(c) One of Foo, Bar, or Baz")
  }

}
