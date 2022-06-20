
package com.mercerenies.werewolf
package http

import java.net.HttpURLConnection
import java.io.{InputStream, ByteArrayInputStream}
import java.nio.charset.StandardCharsets

class HttpResponseSpec extends UnitSpec {

  private def stringInputStream(s: String): InputStream =
    ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8))

  "The HttpResponse companion object" should "construct an HttpResponse from an HttpURLConnection" in {
    val conn: HttpURLConnection = mock
    when(conn.getResponseCode()).thenReturn(999)
    when(conn.getInputStream()).thenAnswer { _ => stringInputStream("abc\nfoobar") }

    val result = HttpResponse.fromConnection(conn)
    val body = new String(result.responseBody, StandardCharsets.UTF_8)
    result.statusCode should be (999)
    body should be ("abc\nfoobar")

    verify(conn).getResponseCode()
    verify(conn).getInputStream()

  }

}
