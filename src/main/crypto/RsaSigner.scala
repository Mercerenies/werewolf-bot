
package com.mercerenies.werewolf
package crypto

import java.security.Signature

class RsaSigner(private val key: PrivateKey) {

  private val signature: Signature = Signature.getInstance("SHA256withRSA")

  private val signatureLock: AnyRef = new AnyRef()

  signature.initSign(key.toJava)

  def sign(arr: Array[Byte]): Array[Byte] =
    signatureLock.synchronized {
      signature.update(arr)
      signature.sign()
    }

}
