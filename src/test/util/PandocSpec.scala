
package com.mercerenies.werewolf
package util

class PandocSpec extends UnitSpec {

  "The Pandoc singleton" should "convert HTML text to plaintext" in {
    assume(Pandoc.exists)

    Pandoc.htmlToPlain("abc") should be ("abc\n")
    Pandoc.htmlToPlain("<b>ab</b> c") should be ("AB c\n")

  }

}
