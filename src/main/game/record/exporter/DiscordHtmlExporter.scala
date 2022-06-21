
package com.mercerenies.werewolf
package game
package record
package exporter

import logging.Logging
import id.UserMapping
import util.html.HtmlBuilder
import util.TextDecorator.*
import http.{RequestMethod, UrlEncodedForm, HttpRequests, Header}
import crypto.{PrivateKey, Base64, RsaSigner}

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.TextChannel

import scalaz.*
import Scalaz.*

import java.net.URL
import java.util.UUID
import java.nio.charset.StandardCharsets

import scala.concurrent.{Future, ExecutionContext}
import scala.jdk.FutureConverters.*
import scala.util.{Try, Success, Failure}

// Makes an HTML page and sends it to the given web address.
class DiscordHtmlExporter(
  destinationUrlPrefix: String,
  private val channel: TextChannel,
) extends HtmlExporter(destinationUrlPrefix + "/upload.php") {

  import DiscordHtmlExporter.logger

  private def finalUrl(uuid: String): String = s"${destinationUrlPrefix}/${uuid}.html"

  override def onSuccess(uuid: String)(using ExecutionContext): Future[Unit] = {
    logger.info(s"Successfully uploaded game ${uuid} via ${destinationUrl}")
    channel.sendMessage(bold("Game logs are now available: ") + finalUrl(uuid)).asScala.void
  }

  override def onFailure(throwable: Throwable)(using ExecutionContext): Future[Unit] = {
    logger.catching(throwable)
    channel.sendMessage(s"Oops! I got an error trying to upload the game logs to ${destinationUrl}. Please ask ${BotConfig.ADMIN_NAME} to look at the server logs.").asScala.void
  }

}

object DiscordHtmlExporter extends Logging[DiscordHtmlExporter]
