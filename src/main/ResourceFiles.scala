
package com.mercerenies.werewolf

import org.javacord.api.{DiscordApi, DiscordApiBuilder}

import org.apache.commons.io.IOUtils

import java.io.InputStream
import java.nio.charset.StandardCharsets

import scala.io.Source
import scala.concurrent.Future
import scala.jdk.FutureConverters.*

object ResourceFiles {

  private val classLoader: ClassLoader =
    this.getClass().getClassLoader()

  def readResource(name: String): String =
    IOUtils.toString(classLoader.getResourceAsStream(name), StandardCharsets.UTF_8)

}
