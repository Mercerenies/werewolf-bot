
package com.mercerenies.werewolf
package util

class CellSpec extends UnitSpec {

  "The Cell class" should "store a value and then allow access to that value later" in {
    val cell = Cell[Int](100)
    cell.value should be (100)
    cell.value = 200
    cell.value should be (200)
    cell.replace(300)
    cell.value should be (300)
  }

  it should "apply a function to the stored value with 'modify'" in {
    val cell = Cell[Int](100)
    cell.modify { _ * 3 + 1 }
    cell.value should be (301)
  }

}
