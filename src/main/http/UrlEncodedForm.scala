
package com.mercerenies.werewolf
package http

import java.nio.ByteBuffer

// Helpers for constructing a message body in the
// application/x-www-form-urlencoded content type.
object UrlEncodedForm {

  val CONTENT_TYPE: String =
    "application/x-www-form-urlencoded"

  // Precondition: All keys and values in this array are already safe
  // for URLs. *No* escaping is done by this function.
  def formData(data: Map[String, Array[Byte]]): Array[Byte] = {
    /////
  }

}
