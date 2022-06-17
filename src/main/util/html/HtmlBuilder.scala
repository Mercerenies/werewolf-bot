
package com.mercerenies.werewolf
package util
package html

import org.apache.commons.text.StringEscapeUtils

// Helpers for building HTML.
object HtmlBuilder {

  def begin(block: (HtmlFragment) ?=> Unit): String = {
    val builder = StringBuilderHtmlFragment()
    block(using builder)
    builder.mkString
  }

  def t(text: String)(using fragment: HtmlFragment): Unit = {
    fragment.append(StringEscapeUtils.escapeHtml4(text))
  }

  val html = HtmlElement("html")
  val head = HtmlElement("head")
  val body = HtmlElement("body")
  val ul = HtmlElement("ul")
  val li = HtmlElement("li")
  val b = HtmlElement("b")

}
