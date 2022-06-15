
package com.mercerenies.werewolf
package choice

import name.{NamedEntity, SimpleName, NoValue}
import ChoiceError.*
import syntax.*

class ChoiceSpec extends UnitSpec {

  val foo = SimpleName("Foo")
  val bar = SimpleName("Bar")
  val baz = SimpleName("Baz")

  val options = List(foo, bar, baz)

  val number1 = SimpleName("1")
  val number2 = SimpleName("2")
  val number3 = SimpleName("3")

  val numOptions = List(number1, number2, number3)

  "The Choice trait" should "never successfully parse on NoChoice" in {
    NoChoice.parse("ABC") should be (Left(NoFurtherOptions))
    NoChoice.parse("") should be (Left(NoFurtherOptions))
    NoChoice.parse("--") should be (Left(NoFurtherOptions))
    NoChoice.parse("foobarbaz") should be (Left(NoFurtherOptions))
    NoChoice.parse("100") should be (Left(NoFurtherOptions))
  }

  it should "parse 'None' on the NoValue choice" in {
    noValue.parse("None") should be (Right(NoValue))
    noValue.parse("none") should be (Right(NoValue))
    noValue.parse("abc none def") should be (Right(NoValue))
    noValue.parse("nonenon") should be (Right(NoValue))
    noValue.parse("NoNENoN") should be (Right(NoValue))
    noValue.parse("NoNNoN") should be (Left(NoFurtherOptions))
    noValue.parse("none none none") should be (Left(WrongNumber(1, 3)))
  }

  it should "parse from a finite collection of options" in {
    val parser = syntax.oneOf(options)
    parser.parse("i am a foo") should be (Right(foo))
    parser.parse("the BAR is here") should be (Right(bar))
    parser.parse("bazme") should be (Right(baz))
    parser.parse("barbaz") should be (Left(WrongNumber(1, 2)))
    parser.parse("---") should be (Left(NoFurtherOptions))
    parser.parse("") should be (Left(NoFurtherOptions))
  }

  it should "parse multiple options from a finite collection of options" in {
    val parser = syntax.twoOf(options)
    parser.parse("foo bar") should be (Right((foo, bar)))
    parser.parse("baz ewuei4hriuwgfygr foo") should be (Right((baz, foo)))
    parser.parse("baz foo bar") should be (Left(WrongNumber(2, 3)))
    parser.parse("baz") should be (Left(WrongNumber(2, 1)))
    parser.parse("baz baz") should be (Left(RepeatedElement))
  }

  it should "parse from disjunctive collections of choices" in {
    val parser = syntax.oneOf(options) :+: syntax.twoOf(numOptions)
    parser.parse("e") should be (Left(NoFurtherOptions))
    parser.parse("foo") should be (Right( Left(foo) ))
    parser.parse("aaa bar bbb") should be (Right( Left(bar) ))
    parser.parse("aaa bar bar bbb") should be (Left(NoFurtherOptions))
    parser.parse("aaa 1 2 bbb") should be (Right( Right((number1, number2)) ))
    parser.parse("aaa 1 1 bbb") should be (Left(RepeatedElement))
    parser.parse("aaa bar 1 bbb") should be (Right( Left(bar) ))
    parser.parse("aaa bar baz 1 2 bbb") should be (Right( Right((number1, number2)) ))
  }

}
