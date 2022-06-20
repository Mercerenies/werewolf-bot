
package com.mercerenies.werewolf
package crypto

import java.nio.charset.StandardCharsets
import java.nio.{CharBuffer, ByteBuffer}

opaque type Utf8String = Array[Byte]

object Utf8String {

  private val charset = StandardCharsets.UTF_8

  def apply(x: String): Utf8String = {
    val encoder = charset.newEncoder()
    val buf = CharBuffer.wrap(x)
    encoder.encode(buf).array()
  }

  extension (self: Utf8String)

    def mkString: String = {
      val decoder = charset.newDecoder()
      val buf = ByteBuffer.wrap(self)
      decoder.decode(buf).toString
    }

    def toArray: IArray[Byte] =
      IArray.unsafeFromArray(self)

}
