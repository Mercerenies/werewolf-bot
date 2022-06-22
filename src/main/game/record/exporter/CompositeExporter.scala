
package com.mercerenies.werewolf
package game
package record
package exporter

import id.UserMapping
import util.foldM

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
      allErrors <- runEach(record, userMapping)
      _ <- if (allErrors.isEmpty) { Future.unit } else { Future.failed(CompositeException(allErrors.reverse)) }
    } yield {
      ()
    }
  }

  // Note: It is tempting to think of this fold as a traversal (and
  // indeed, the original version of this code did just that), but a
  // traversal is capable of running the exporters in parallel,
  // whereas I want to guarantee that they run sequentially,
  // accumulating errors as we go.
  private def runEach(record: RecordedGameHistory, userMapping: UserMapping)(using ExecutionContext): Future[List[Throwable]] =
    foldM(exporters, (Nil: List[Throwable])) { (acc, x) =>
      CompositeExporter.recoverFuture(x.exportRecord(record, userMapping)) map {
        case None => acc
        case Some(err) => err :: acc
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
