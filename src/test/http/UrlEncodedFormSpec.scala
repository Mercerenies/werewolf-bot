
package com.mercerenies.werewolf
package http

import java.nio.charset.StandardCharsets

class UrlEncodedFormSpec extends UnitSpec {

  // Convenience function which takes strings instead of byte arrays,
  // and returns a string, for testing purposes.
  private def encodeData(data: (String, String)*): String = {
    val iter = data.map { (k, v) => (k, v.getBytes(StandardCharsets.UTF_8)) }
    new String(UrlEncodedForm.formData(iter), StandardCharsets.UTF_8)
  }

  "The UrlEncodedForm helper" should "encode data in the x-www-form-urlencoded content type" in {
    encodeData() should be ("")
    encodeData("a" -> "b") should be ("a=b")
    encodeData("a" -> "b", "foo" -> "bar") should be ("a=b&foo=bar")
    encodeData("a" -> "b", "foo" -> "bar", "c" -> "3") should be ("a=b&foo=bar&c=3")
  }

}
