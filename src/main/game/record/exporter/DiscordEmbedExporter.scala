
package com.mercerenies.werewolf
package game
package record
package exporter

import id.UserMapping
import util.Pandoc
import util.TextDecorator.*
import util.html.{HtmlBuilder, HtmlFragment}
import http.{RequestMethod, UrlEncodedForm, HttpRequests, Header}
import crypto.{PrivateKey, Base64, RsaSigner}
import logging.Logging

import org.javacord.api.DiscordApi
import org.javacord.api.entity.channel.TextChannel

import java.net.URL
import java.util.{UUID, Date}
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.io.ByteArrayInputStream

import scala.concurrent.{Future, ExecutionContext}
import scala.jdk.FutureConverters.*
import scala.util.{Try, Success, Failure}

// Embeds the plaintext output into a Discord message.
class DiscordEmbedExporter(
  private val channel: TextChannel,
) extends RecordExporter {
  import DiscordEmbedExporter.logger

  def exportRecord(record: RecordedGameHistory, userMapping: UserMapping)(using ExecutionContext): Future[Unit] =
    if (Pandoc.exists) {
      exportRecordUnchecked(record, userMapping)
    } else {
      // If Pandoc does not exist, then do not use this export.
      logger.debug("Pandoc does not exist, DiscordEmbedExporter is exiting.")
      return Future.successful(())
    }

  private def exportRecordUnchecked(record: RecordedGameHistory, userMapping: UserMapping)(using ExecutionContext): Future[Unit] = {
    val htmlPage: String = HtmlBuilder.begin {
      this.toHtml {
        HtmlBuilder.ul {
          record.foreach { _.htmlText(userMapping) }
        }
      }
    }

    for {
      plaintext <- Future { Pandoc.htmlToPlain(htmlPage) } // Catch errors during Pandoc into the future invocation
      plaintextStream = ByteArrayInputStream(plaintext.getBytes(StandardCharsets.UTF_8))
      _ <- channel.sendMessage(bold("You may download the logs here."), plaintextStream, "logs.txt").asScala
    } yield {
      ()
    }

  }

  private def toHtml(gameBody: => Unit)(using HtmlFragment): Unit = {
    HtmlExporter.defaultToHtml(gameBody)
  }

}

object DiscordEmbedExporter extends Logging[DiscordEmbedExporter]
