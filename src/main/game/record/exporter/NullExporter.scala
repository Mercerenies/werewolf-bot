
package com.mercerenies.werewolf
package game
package record
package exporter

import id.UserMapping

import scala.concurrent.{Future, ExecutionContext}

object NullExporter extends RecordExporter {

  def exportRecord(record: RecordedGameHistory, userMapping: UserMapping)(using ExecutionContext): Future[Unit] = {
    // This method intentionally left blank.
    Future.successful(())
  }

}
