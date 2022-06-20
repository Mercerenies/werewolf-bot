
package com.mercerenies.werewolf
package crypto

import util.tr

import java.util.{Base64 as JBase64}

// Utilities for converting to base64, with some minor modifications
// made for (a) URL compatibility and (b) PHP support.
object Base64 {

  private val encoder = JBase64.getEncoder()
  private val decoder = JBase64.getDecoder()

  // Takes a char->char translation table and produces a byte->byte
  // one.
  private def byteTranslationTable(m: Map[Char, Char]): Map[Byte, Byte] =
    m.map { (k, v) => (k.toByte, v.toByte) }

  // Does not modify the argument array.
  def encode(x: Array[Byte]): Array[Byte] = {
    val buf = encoder.encode(x)
    val trans = byteTranslationTable(tr("+/=", "._-"))
    buf.mapInPlace { b => trans.getOrElse(b, b) }
  }

  // Does not modify the argument array.
  def decode(x: Array[Byte]): Array[Byte] = {
    val buf = decoder.decode(x)
    val trans = byteTranslationTable(tr("._-", "+/="))
    buf.mapInPlace { b => trans.getOrElse(b, b) }
  }

}
