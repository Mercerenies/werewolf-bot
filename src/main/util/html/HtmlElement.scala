
package com.mercerenies.werewolf
package util
package html

import scala.util.matching.Regex

// Precondition: tagName shall be nonempty and shall contain only
// alphabetical characters.
class HtmlElement(val tagName: String) {

  if (!HtmlElement.TAG_NAME_VALIDATOR.matches(tagName)) {
    throw new IllegalArgumentException(s"Bad tag name ${tagName}")
  }

  def apply(body: (HtmlFragment) ?=> Unit)(using fragment: HtmlFragment): Unit = {
    fragment.append(s"<${tagName}>")
    try {
      body(using fragment)
    } finally {
      fragment.append(s"</${tagName}>")
    }
  }

}

object HtmlElement {

  private val TAG_NAME_VALIDATOR: Regex =
    raw"^[A-Za-z]+$$".r

}
