
package com.mercerenies.werewolf
package game
package parser

class ParsingSpec extends UnitSpec {

  "The Parsing singleton" should "find long code blocks in a block of text" in {
    val text = "```incode```\nout of code\n```incode2\nincode3```\nout of code"
    Parsing.findLongCodeBlocks(text) should be (List(
      "incode",
      "incode2\nincode3",
    ))
  }

  it should "find bold text in a block of text" in {
    val text = "**bold** *italic* ~~strikethrough~~ **more bold\nmultiline**"
    Parsing.findBoldText(text) should be (List(
      "bold",
      "more bold\nmultiline",
    ))
  }

}
