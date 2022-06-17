
package com.mercerenies.werewolf
package util
package html

// As StringBuilderHtmlFragment but ignores HTML tags. Useful for
// producing plaintext output.
class PlaintextBuilderHtmlFragment extends HtmlFragment {
  private val builder: StringBuilder = StringBuilder()

  def append(text: String): Unit =
    builder.append(text) // Plaintext so no escaping needed

  def appendTag(text: String): Unit = {
    // Do nothing; we're exporting to plaintext
  }

  def mkString: String =
    builder.toString

}
