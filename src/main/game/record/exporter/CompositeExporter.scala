
package com.mercerenies.werewolf
package game
package record
package exporter

import id.UserMapping

import scalaz.*
import Scalaz.*

import scala.util.{Try, Success, Failure}
import scala.concurrent.{Future, ExecutionContext}

// A composite of several exporters. Each one will run independently
// and has an independent ability to fail. In case of failure, all
// errors are wrapped in a CompositeException.
class CompositeExporter(
  private val exporters: List[RecordExporter],
) extends RecordExporter {

  def this(exporters: RecordExporter*) = this(exporters.toList)

  def exportRecord(record: RecordedGameHistory, userMapping: UserMapping)(using ExecutionContext): Future[Unit] = {
    // We want to run all the exporters, capturing (but not failing
    // on) any errors that occur. Then we want to check and see if
    // there are any errors and, if so, accumulate them all into one
    // big CompositeException.
    for {
      allExports: List[Option[Throwable]] <- exporters.traverse { x => CompositeExporter.recoverFuture(x.exportRecord(record, userMapping)) }
      allErrors: List[Throwable] = allExports.flatten
      _ <- if (allErrors.isEmpty) { Future.unit } else { Future.failed(CompositeException(allErrors)) }
    } yield {
      ()
    }
  }

}

object CompositeExporter {

  // In case of success, return a future containing None. In case of
  // failure, return a future which succeeded with the given
  // Throwable. The future returned from this function is always
  // successful.
  private def recoverFuture[U](value: Future[U])(using ExecutionContext): Future[Option[Throwable]] =
    value.transform {
      case Failure(err) => Success(Some(err))
      case Success(_) => Success(None)
    }

}
