
package com.mercerenies.werewolf
package util
package html

import scala.util.matching.Regex

// Precondition: tagName shall be nonempty and shall contain only
// alphabetical characters.
case class HtmlElement(val tagName: String) {

  if (!HtmlElement.TAG_NAME_VALIDATOR.matches(tagName)) {
    throw new IllegalArgumentException(s"Bad tag name ${tagName}")
  }

  def openingTag(attrs: Iterable[Attribute]): String = {
    val attrText = attrs.map { attr => " " + attr.mkString }.mkString
    s"<${tagName}${attrText}>"
  }

  def closingTag: String =
    s"</${tagName}>"

  def apply(body: (HtmlFragment) ?=> Unit)(using HtmlFragment): Unit =
    attr()(body)

  def attr(attrs: Attribute*)(body: (HtmlFragment) ?=> Unit)(using fragment: HtmlFragment): Unit = {
    fragment.append(openingTag(attrs))
    try {
      body(using fragment)
    } finally {
      fragment.append(closingTag)
    }
  }

}

object HtmlElement {

  private val TAG_NAME_VALIDATOR: Regex =
    raw"^[A-Za-z]+$$".r

}
