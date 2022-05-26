
package com.mercerenies.werewolf

import org.javacord.api.{DiscordApi, DiscordApiBuilder}

import scala.io.Source
import scala.concurrent.Future
import scala.jdk.FutureConverters.*

final class BotConfig(
  private val tokenFilename: String = "token.txt",
) {

  val token: String =
    Source.fromFile(tokenFilename).mkString.stripLineEnd

  def produceApi(): Future[DiscordApi] =
    DiscordApiBuilder().setToken(token).login().asScala

}
