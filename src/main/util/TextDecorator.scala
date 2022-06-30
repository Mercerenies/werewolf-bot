
package com.mercerenies.werewolf
package util

import org.javacord.api.entity.message.MessageDecoration

// Helpers for decorating text in preparation to be interpreted as
// Markdown.
case class TextDecorator(val prefix: String, val suffix: String) {

  def apply(x: String): String =
    prefix + x + suffix

}

object TextDecorator {

  def fromMessageDecoration(decorator: MessageDecoration): TextDecorator =
    TextDecorator(decorator.getPrefix, decorator.getSuffix)

  val bold = fromMessageDecoration(MessageDecoration.BOLD)
  val longCode = fromMessageDecoration(MessageDecoration.CODE_LONG)
  val code = fromMessageDecoration(MessageDecoration.CODE_SIMPLE)
  val italic = fromMessageDecoration(MessageDecoration.ITALICS)
  val spoiler = fromMessageDecoration(MessageDecoration.SPOILER)
  val strikeout = fromMessageDecoration(MessageDecoration.STRIKEOUT)
  val underline = fromMessageDecoration(MessageDecoration.UNDERLINE)

  def bulletedList(items: List[String]): String =
    items.map { "• " ++ _ }.mkString("\n")

}
