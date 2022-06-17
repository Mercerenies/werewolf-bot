
package com.mercerenies.werewolf
package util
package html

class StringBuilderHtmlFragment extends HtmlFragment {
  private val builder: StringBuilder = StringBuilder()

  def append(text: String): Unit =
    builder.append(text)

  def mkString: String =
    builder.toString

}
