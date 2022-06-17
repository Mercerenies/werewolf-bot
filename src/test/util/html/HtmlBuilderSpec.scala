
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

  it should "allow attributes to be used via the DSL" in {
    begin {
      body {
        b.attr("style" := "potato") { t("Bold text") }
        b.attr("style" := "\"") { t("Bold text") }
      }
    } should be ("<body><b style=\"potato\">Bold text</b><b style=\"&quot;\">Bold text</b></body>")
  }

  it should "allow multiple attributes to be used via the DSL" in {
    begin {
      body {
        b.attr() { t("Bold text") } // Explicit empty attribute list
        b.attr("a" := "a", "b" := "b") { t("Bold text") }
      }
    } should be ("<body><b>Bold text</b><b a=\"a\" b=\"b\">Bold text</b></body>")
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

  it should "suppress tags entirely when using the plaintext export" in {
    beginPlaintext {
      body {
        b.attr("style" := "potato") { t("Bold text") }
        b.attr("style" := "\"") { t("Bold text") }
      }
    } should be ("Bold textBold text")
  }

  it should "not escape HTML metacharacters when using the plaintext export" in {
    beginPlaintext {
      body {
        b { t("&&\"&&<b>") }
      }
    } should be ("&&\"&&<b>")
  }

}
