
package com.mercerenies.werewolf
package crypto

class Base64Spec extends UnitSpec {

  private def roundTrip(x: Array[Byte]): Array[Byte] =
    Base64.decode(Base64.encode(x))

  private def encodeString(x: String): String =
    new String(Base64.encode(x.getBytes("UTF-8")), "UTF-8")

  private def decodeString(x: String): String =
    new String(Base64.decode(x.getBytes("UTF-8")), "UTF-8")

  "The Base64 singleton" should "round-trip byte arrays through encoding and decoding" in {

    val x: Array[Byte] = Array(1, 2, 3, 0, 0, 88, 65, 9, 0, 1, 2, 2, 1, -127, -126, -1)
    roundTrip(x) should be (x)

    val y: Array[Byte] = Array(1, 2, 3, 0, 0, 88, 65, 9, 0, 1, 2, 2, 1, -127, -126, -1, 0)
    roundTrip(y) should be (y)

    val z: Array[Byte] = Array()
    roundTrip(z) should be (z)

  }

  it should "encode a string into base64 with URL-unsafe characters replaced" in {
    encodeString("a") should be ("YQ--")
    encodeString("abc") should be ("YWJj")
  }

  it should "decode a string from base64" in {
    decodeString("YQ--") should be ("a")
    decodeString("YWJj") should be ("abc")

  }

}
