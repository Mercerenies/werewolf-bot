
package com.mercerenies.werewolf
package game
package night

import SelectionConfirmFormatter.format

class SelectionConfirmFormatterSpec extends UnitSpec {

  class CustomObject(val n: Int) {
    override def toString: String = s"CustomObject(${n})"
  }

  "SelectionConfirmFormatter" should "format custom objects using toString" in {
    format(CustomObject(10)) should be ("CustomObject(10)")
  }

  it should "format built-in objects using toString" in {
    format(1099) should be ("1099")
    format("foobar") should be ("foobar")
  }

  it should "format the inside of Either" in {
    val obj1: Either[Int, String] = Left(0)
    val obj2: Either[Int, String] = Right("potato")
    format(obj1) should be ("0")
    format(obj2) should be ("potato")
  }

  it should "format the inside of nested Either" in {
    val obj1: Either[Int, Either[String, String]] = Right(Right("potato"))
    format(obj1) should be ("potato")
  }

  it should "format lists of objects in a comma-separated way" in {
    format(List()) should be ("")
    format(List(1)) should be ("1")
    format(List(1, 2)) should be ("1 and 2")
    format(List(1, 2, 3)) should be ("1, 2, and 3")
    format(List(1, 2, 3, 4)) should be ("1, 2, 3, and 4")
  }

  it should "format tuples of objects in a comma-separated way" in {
    format(EmptyTuple) should be ("")
    format(Tuple1(100)) should be ("100")
    format((1, "ABC")) should be ("1 and ABC")
    format((1, "ABC", CustomObject(1))) should be ("1, ABC, and CustomObject(1)")
  }

}
