
package com.mercerenies.werewolf
package timer

import logging.Logs.logErrors

import org.apache.logging.log4j.Logger

import scala.util.Try

class ErrorLoggingRunnable(
  private val task: Runnable,
  private val logger: Logger,
) extends Runnable {

  override def run(): Unit = {
    Try { task.run() }.logErrors(logger)
  }

}
