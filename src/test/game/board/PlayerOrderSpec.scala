
package com.mercerenies.werewolf
package game
package board

import IterableNormalizations.unordered
import id.Id

import org.javacord.api.entity.user.User

class PlayerOrderSpec extends UnitSpec {

  private def id(x: Long): Id[User] =
    Id.fromLong(x)

  "The PlayerOrder helper class" should "allow conversion to list with toList" in {
    val list = List(id(10), id(100), id(3), id(4))
    PlayerOrder(list).toList should be (list)
    PlayerOrder(list).players should be (list)
  }

  it should "not allow construction of an empty PlayerOrder" in {
    an [IllegalArgumentException] should be thrownBy PlayerOrder(Nil)
  }

  it should "not allow duplicates" in {
    an [IllegalArgumentException] should be thrownBy PlayerOrder(List(id(0), id(0)))
  }

  it should "allow typical streaming operations with .map" in {
    val list = List(id(10), id(100), id(3), id(4))
    PlayerOrder(list).map { _.toLong } should be (List(10, 100, 3, 4))
  }

  it should "allow typical streaming operations with .filter" in {
    val list = List(id(10), id(100), id(3), id(4))
    PlayerOrder(list).filter { _.toLong % 10 == 0 } should be (List(id(10), id(100)))
  }

  it should "return the index of the given user with indexOf, or return -1 if not found" in {
    val list = List(id(10), id(100), id(3), id(4))
    val order = PlayerOrder(list)
    order.indexOf(id(10)) should be (0)
    order.indexOf(id(100)) should be (1)
    order.indexOf(id(3)) should be (2)
    order.indexOf(id(4)) should be (3)
    order.indexOf(id(5)) should be (-1)
  }

  it should "return the index of the given user with indexOfChecked, or throw an error if nonexistent" in {
    val list = List(id(10), id(100), id(3), id(4))
    val order = PlayerOrder(list)
    order.indexOfChecked(id(10)) should be (0)
    order.indexOfChecked(id(100)) should be (1)
    order.indexOfChecked(id(3)) should be (2)
    order.indexOfChecked(id(4)) should be (3)
    a [NoSuchElementException] should be thrownBy order.indexOfChecked(id(5))
  }

  it should "return the ID of adjacent users" in {
    val list = List(id(10), id(100), id(3), id(4))
    val order = PlayerOrder(list)

    order.leftOf(id(10)) should be (id(4))
    order.leftOf(id(100)) should be (id(10))
    order.leftOf(id(3)) should be (id(100))
    order.leftOf(id(4)) should be (id(3))

    order.rightOf(id(10)) should be (id(100))
    order.rightOf(id(100)) should be (id(3))
    order.rightOf(id(3)) should be (id(4))
    order.rightOf(id(4)) should be (id(10))

    order.adjacentPlayers(id(10)) should be ((id(4), id(100)))
    order.adjacentPlayers(id(100)) should be ((id(10), id(3)))
    order.adjacentPlayers(id(3)) should be ((id(100), id(4)))
    order.adjacentPlayers(id(4)) should be ((id(3), id(10)))

    a [NoSuchElementException] should be thrownBy order.leftOf(id(999))
    a [NoSuchElementException] should be thrownBy order.rightOf(id(999))
    a [NoSuchElementException] should be thrownBy order.adjacentPlayers(id(999))

  }

}
