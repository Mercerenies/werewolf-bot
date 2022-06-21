
package com.mercerenies.werewolf
package game
package record
package exporter

import id.UserMapping

import java.io.PrintStream

import scala.concurrent.{Future, ExecutionContext}

class PlaintextExporter(
  private val stream: PrintStream,
) extends RecordExporter {

  def exportRecord(record: RecordedGameHistory, userMapping: UserMapping)(using ExecutionContext): Future[Unit] = {
    // Go ahead and just do it synchronously, since this is probably
    // writing to stdout or some other "quick" stream and doesn't need
    // to spin up a thread or anthing.
    record.foreach { record =>
      stream.println(record.displayText(userMapping))
    }
    Future.successful(())
  }

}
