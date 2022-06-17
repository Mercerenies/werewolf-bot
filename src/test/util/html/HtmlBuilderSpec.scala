
package com.mercerenies.werewolf
package util
package html

class HtmlBuilderSpec extends UnitSpec {
  import HtmlBuilder.*

  "The HtmlBuilder DSL" should "build HTML from plaintext" in {
    begin { t("ABC") } should be ("ABC")
    begin { t("foo bar\nbaz") } should be ("foo bar\nbaz")
    begin { t("foo ") ; t("bar\nbaz") } should be ("foo bar\nbaz")
  }

  it should "escape plaintext which would not be valid HTML" in {
    begin { t("&") } should be ("&amp;")
    begin { t("&amp;") } should be ("&amp;amp;")
    begin { t("<b>") } should be ("&lt;b&gt;")
  }

  it should "allow tags to be used via the DSL" in {
    begin {
      html {
        head { t("sample&sample") }
        body {
          t("Regular text ")
          b { t("Bold text") }
        }
      }
    } should be ("<html><head>sample&amp;sample</head><body>Regular text <b>Bold text</b></body></html>")
  }

  it should "close tags even if an exception is thrown on the inside" in {
    begin {
      try {
        body {
          t("1")
          t("2")
          throw new RuntimeException("The DSL stops here")
          t("3")
        }
      } catch {
        case _: RuntimeException => {}
      }
    } should be ("<body>12</body>")
  }

}
