
package com.mercerenies.werewolf
package game
package record
package exporter

import id.UserMapping
import util.html.HtmlBuilder
import http.{RequestMethod, UrlEncodedForm, HttpRequests, Header}
import crypto.{PrivateKey, Base64, RsaSigner}

import java.net.URL
import java.util.UUID
import java.nio.charset.StandardCharsets

import scala.concurrent.{Future, ExecutionContext}
import scala.util.{Try, Success, Failure}

// Makes an HTML page and sends it to the given web address.
open class HtmlExporter(
  val destinationUrl: URL,
) extends RecordExporter {

  def this(destinationUrl: String) = this(new URL(destinationUrl))

  final def exportRecord(record: RecordedGameHistory, userMapping: UserMapping)(using ExecutionContext): Future[Unit] = {
    val fullPage: String = HtmlBuilder.begin { record.foreach { _.htmlText(userMapping) } }
    val header = Header("Content-Type" -> "application/x-www-form-urlencoded")

    val uuid = HtmlExporter.generateUuid()
    val body = HtmlExporter.buildMessageBody(page = fullPage, uuid = uuid)

    HttpRequests.makeRequest(destinationUrl, RequestMethod.POST, header, body).transformWith {
      case Failure(err) => onFailure(err)
      case Success(_) => onSuccess(uuid)
    }

  }

  def onSuccess(uuid: String)(using ExecutionContext): Future[Unit] = Future.unit

  def onFailure(throwable: Throwable)(using ExecutionContext): Future[Unit] = Future.unit

}

object HtmlExporter {

  private def generateUuid(): String =
    UUID.randomUUID().toString()

  private def buildMessageBody(page: String, uuid: String): Array[Byte] = {
    val pageBytes: Array[Byte] = page.getBytes(StandardCharsets.UTF_8)

    val pkey = loadPrivateKey()
    val signature = signData(pageBytes, pkey)
    val encoded = encodeData(pageBytes)

    UrlEncodedForm.formData(
      "targetname" -> uuid.getBytes(StandardCharsets.UTF_8),
      "signature" -> signature,
      "data" -> encoded,
    )
  }

  private def loadPrivateKey(): PrivateKey = {
    val pkeyFile = ResourceFiles.readResourceAsBytes("private.der")
    PrivateKey.fromBytes(pkeyFile)
  }

  private def signData(page: Array[Byte], key: PrivateKey): Array[Byte] = {
    val signer = RsaSigner(key)
    val signed = signer.sign(page)
    Base64.encode(signed)
  }

  private def encodeData(page: Array[Byte]): Array[Byte] =
    Base64.encode(page)

}
