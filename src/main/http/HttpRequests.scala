
package com.mercerenies.werewolf
package http

import java.net.{URL, HttpURLConnection}

import scala.concurrent.{Future, ExecutionContext}

object HttpRequests {

  // Precondition: url is an HTTP URL.
  def makeRequestSync(url: URL, method: RequestMethod, header: Header, body: Array[Byte]): HttpResponse = {
    val conn = url.openConnection.asInstanceOf[HttpURLConnection]
    conn.setDoOutput(true)
    conn.setRequestMethod(method.toString)
    header.toList.foreach { (k, v) =>
      conn.setRequestProperty(k, v)
    }

    // Write the data to the connection
    val wr = conn.getOutputStream()
    wr.write(body)
    wr.flush()
    wr.close()

    // Now get the response
    HttpResponse.fromConnection(conn)

  }

  // Precondition: url is an HTTP URL.
  def makeRequest(url: URL, method: RequestMethod, header: Header, body: Array[Byte])(using ExecutionContext): Future[HttpResponse] =
    Future { makeRequestSync(url, method, header, body) }

}
