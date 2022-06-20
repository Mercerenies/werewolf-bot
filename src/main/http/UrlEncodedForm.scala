
package com.mercerenies.werewolf
package http

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import scala.collection.mutable.ArrayBuffer

// Helpers for constructing a message body in the
// application/x-www-form-urlencoded content type.
object UrlEncodedForm {

  val CONTENT_TYPE: String =
    "application/x-www-form-urlencoded"

  private val EQUALS_SIGN: Array[Byte] =
    encode("=")

  private val AMPERSAND: Array[Byte] =
    encode("&")

  // Precondition: All keys and values in this array are already safe
  // for URLs. *No* escaping is done by this function.
  def formData(data: Iterable[(String, Array[Byte])]): Array[Byte] = {
    val buf = new ArrayBuffer[Byte](256)
    var first = true
    data.foreach { (k, v) =>
      if (!first) {
        buf ++= AMPERSAND
      }
      buf ++= encode(k)
      buf ++= EQUALS_SIGN
      buf ++= v
      first = false
    }
    buf.toArray
  }

  def formData(data: (String, Array[Byte])*): Array[Byte] =
    formData(data)

  private def encode(s: String): Array[Byte] =
    s.getBytes(StandardCharsets.UTF_8)

}
