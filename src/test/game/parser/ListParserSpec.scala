
package com.mercerenies.werewolf
package game
package parser

import name.NamedEntity

import scalaz.*
import Scalaz.*

class ListParserSpec extends UnitSpec {

  case class TestEntity(val n: Int) extends NamedEntity {
    val name = s"Entity${n}"
    val aliases = List(s"e${n}")
  }

  val entity1 = TestEntity(1)
  val entity2 = TestEntity(2)
  val entity3 = TestEntity(3)

  val parser = ListParser(List(entity1, entity2, entity3), "object")

  "A ListParser" should "find code blocks containing known entity names" in {
    val text = "e3\n```e1\ne2\nEntity2```"
    parser.parse(text) should be (List(entity1, entity2, entity2).right)
  }

  it should "allow blank lines in code blocks" in {
    val text = "```\n\ne1\n\ne2\n\n\n\n```"
    parser.parse(text) should be (List(entity1, entity2).right)
  }

  it should "allow multiple code blocks and concatenate the results" in {
    val text = "```\ne1\ne2```\nabc\n```Entity3```"
    parser.parse(text) should be (List(entity1, entity2, entity3).right)
  }

  it should "fail if given no code blocks" in {
    val text = "foobar"
    parser.parse(text).isLeft should be (true)
  }

  it should "succeed if given only empty code blocks" in {
    val text = "```\n``` foobar ```\n\n\n\n\n```"
    parser.parse(text).isRight should be (true)
  }

  it should "fail if given an unknown name in a code block" in {
    val text = "```e4```"
    parser.parse(text).isLeft should be (true)
  }

}
