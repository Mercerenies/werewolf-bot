
package com.mercerenies.werewolf
package crypto

import java.security.{KeyFactory, PrivateKey as JPrivateKey}
import java.security.spec.PKCS8EncodedKeySpec

class PrivateKey private(
  private val key: JPrivateKey,
) {

  def toJava: JPrivateKey =
    key

}

object PrivateKey {

  private val keyFactory: KeyFactory =
    KeyFactory.getInstance("RSA")

  def fromBytes(bytes: Array[Byte]): PrivateKey = {
    val spec = PKCS8EncodedKeySpec(bytes)
    val jkey = keyFactory.generatePrivate(spec)
    PrivateKey(jkey)
  }

  def fromBytes(bytes: IterableOnce[Byte]): PrivateKey =
    fromBytes(bytes.toArray)

}
