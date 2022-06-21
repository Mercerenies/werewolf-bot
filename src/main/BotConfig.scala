
package com.mercerenies.werewolf

import org.javacord.api.{DiscordApi, DiscordApiBuilder}

import scala.io.Source
import scala.concurrent.Future
import scala.jdk.FutureConverters.*

final class BotConfig(
  private val tokenFilename: String = "token.txt",
) {

  val token: String =
    ResourceFiles.readResource(tokenFilename).stripLineEnd

  def produceApi(): Future[DiscordApi] =
    DiscordApiBuilder().setToken(token).login().asScala

}

object BotConfig {

  // The name of the Discord member responsible for maintaining this
  // bot. This mention tag is pinged when certain errors occur.
  //
  // To be perfectly clear, this is ONLY used to ping the bot creator
  // in case of errors. This bot can be installed on any server, and
  // Mercerenies will not be given escalated privileges.
  val ADMIN_NAME: String = "@Mercerenies#4792"

}
