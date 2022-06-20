
package com.mercerenies.werewolf
package http

import org.apache.commons.io.IOUtils

import java.net.HttpURLConnection

case class HttpResponse(
  val statusCode: Int,
  val responseBody: Array[Byte],
)

object HttpResponse {

  def fromConnection(conn: HttpURLConnection): HttpResponse = {
    val statusCode = conn.getResponseCode()
    val in = conn.getInputStream()
    val responseBody = IOUtils.toByteArray(in)
    HttpResponse(statusCode = statusCode, responseBody = responseBody)
  }

}
