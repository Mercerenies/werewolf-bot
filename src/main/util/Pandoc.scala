
package com.mercerenies.werewolf
package util

import java.nio.charset.StandardCharsets
import java.io.ByteArrayInputStream

import scala.sys.process.{ProcessLogger, Process}
import scala.util.Try

// Helper for invoking the pandoc command line tool conveniently.
object Pandoc {

  private val NullProcessLogger =
    ProcessLogger { _ =>
      // Ignore all output.
    }

  private class StringBuilderProcessLogger extends ProcessLogger {
    private var builder: StringBuilder = StringBuilder()

    override def buffer[T](f: => T): T = {
      // Start a new buffer, so this object can be reused for multiple
      // processes.
      this.builder = StringBuilder()
      f
    }

    override def out(s: => String): Unit = {
      // Send stdout to the buffer
      builder.append(s).append("\n")
    }

    override def err(s: => String): Unit = {
      // Print stderr to the owner's stderr
      Console.err.println(s)
    }

    def output: String =
      builder.toString

  }

  def exists: Boolean = {
    val exitCode = Process("pandoc", Seq("-v")) ! NullProcessLogger
    exitCode == 0
  }

  // Throws an exception if Pandoc fails or does not exist. Recommend
  // checking Pandoc.exists before calling this, or invoking
  // tryHtmlToPlain instead, which catches errors.
  def htmlToPlain(html: String): String = {
    val htmlBytes = html.getBytes(StandardCharsets.UTF_8)
    val htmlStream = ByteArrayInputStream(htmlBytes)
    val processLogger = StringBuilderProcessLogger()
    val exitCode = Process("pandoc", Seq("--from=html", "--to=plain")) #< htmlStream ! processLogger
    if (exitCode != 0) {
      throw new RuntimeException(s"pandoc command got exit code ${exitCode}")
    }
    processLogger.output
  }

  def tryHtmlToPlain(html: String): Try[String] =
    Try(htmlToPlain(html))

}
