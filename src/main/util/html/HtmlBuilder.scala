
package com.mercerenies.werewolf
package util
package html

import org.apache.commons.text.StringEscapeUtils

// Helpers for building HTML.
object HtmlBuilder {

  export Attribute.:=

  def escape(x: String): String =
    StringEscapeUtils.escapeHtml4(x)

  def begin(block: (HtmlFragment) ?=> Unit): String = {
    val builder = StringBuilderHtmlFragment()
    block(using builder)
    builder.mkString
  }

  def beginPlaintext(block: (HtmlFragment) ?=> Unit): String = {
    val builder = PlaintextBuilderHtmlFragment()
    block(using builder)
    builder.mkString
  }

  def t(text: String)(using fragment: HtmlFragment): Unit = {
    fragment.append(text)
  }

  val html = HtmlElement("html")
  val head = HtmlElement("head")
  val body = HtmlElement("body")
  val title = HtmlElement("title")
  val h1 = HtmlElement("h1")
  val h2 = HtmlElement("h2")
  val h3 = HtmlElement("h3")
  val ul = HtmlElement("ul")
  val li = HtmlElement("li")
  val b = HtmlElement("b")
  val p = HtmlElement("p")
  val em = HtmlElement("em")
  val strong = HtmlElement("strong")
  val table = HtmlElement("table")
  val thead = HtmlElement("thead")
  val tbody = HtmlElement("tbody")
  val tfoot = HtmlElement("tfoot")
  val tr = HtmlElement("tr")
  val td = HtmlElement("td")
  val th = HtmlElement("th")
  val main = HtmlElement("main")
  val link = HtmlElement("link")
  val div = HtmlElement("div")
  val span = HtmlElement("span")

}
