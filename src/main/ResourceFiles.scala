
package com.mercerenies.werewolf

import org.javacord.api.{DiscordApi, DiscordApiBuilder}

import org.apache.commons.io.IOUtils

import java.io.InputStream
import java.nio.charset.StandardCharsets

import scala.io.Source
import scala.concurrent.Future
import scala.util.Using
import scala.jdk.FutureConverters.*

object ResourceFiles {

  private val classLoader: ClassLoader =
    this.getClass().getClassLoader()

  def readResource(name: String): String =
    Using(classLoader.getResourceAsStream(name)) { stream =>
      IOUtils.toString(stream, StandardCharsets.UTF_8)
    }.get

  def readResourceAsBytes(name: String): Array[Byte] =
    Using(classLoader.getResourceAsStream(name)) { stream =>
      IOUtils.toByteArray(stream)
    }.get

}
