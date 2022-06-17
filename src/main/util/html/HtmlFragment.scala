
package com.mercerenies.werewolf
package util
package html

trait HtmlFragment {

  // Appends text intended to be treated as plaintext, escaping as
  // necessary.
  def append(text: String): Unit

  // Appends, but marks the data as an HTML tag which should be
  // excluded from e.g. plaintext exports, which do not have tags.
  def appendTag(text: String): Unit

}
