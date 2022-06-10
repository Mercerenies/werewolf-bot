
package com.mercerenies.werewolf
package timer

import logging.Logging
import logging.Logs.logErrors

import org.apache.logging.log4j.Logger

import java.util.concurrent.{ScheduledExecutorService, Executors, TimeUnit, Future => JFuture}

import scala.concurrent.duration.Duration
import scala.concurrent.{Promise, Future}
import scala.util.Try

// Timer implementation that uses Scala futures and promises.
// Currently, this timer only supports one-shot tasks.
final class Timer {

  import Timer.logger

  private val service: ScheduledExecutorService =
    Executors.newScheduledThreadPool(2)

  private def scheduleTaskImpl(delay: Duration, task: Runnable): Cancellable = {
    val future = service.schedule(task, delay.toMillis, TimeUnit.MILLISECONDS)
    Timer.JFutureCancellable(future)
  }

  // As scheduleTask, but used when you don't need a Future. Any
  // uncaught exceptions during the task will be logged and
  // suppressed.
  def scheduleTaskCast(delay: Duration)(task: Runnable): Cancellable = {
    val runnable = ErrorLoggingRunnable(task, logger)
    scheduleTaskImpl(delay, runnable)
  }

  // Schedule a one-shot task for execution after the given delay. The
  // task will return a value in the given Future. Any errors will be
  // passed to the Future object.
  //
  // If the task is cancelled, the Future will fail with
  // CancelledTaskException.
  def scheduleTask[A](delay: Duration)(task: () => A): (Future[A], Cancellable) = {
    val runnable = Timer.WithPromiseRunnable(task)
    val cancelAction = scheduleTaskImpl(delay, runnable)
    val wrappedCancelAction = new Cancellable() {
      override def cancel(): Unit = {
        cancelAction.cancel()
        runnable.onTaskCancelled()
      }
    }
    (runnable.future, wrappedCancelAction)
  }

}

object Timer extends Logging[Timer] {

  private final class WithPromiseRunnable[A](
    private val task: () => A,
  ) extends Runnable {

    private val promise: Promise[A] = Promise()

    def future: Future[A] = promise.future

    def onTaskCancelled(): Unit = {
      promise.tryFailure(new CancelledTaskException())
    }

    override def run(): Unit = {
      promise.tryComplete(Try { task() })
    }

  }

  private class JFutureCancellable(
    private val jfuture: JFuture[?],
  ) extends Cancellable {

    override def cancel(): Unit = {
      jfuture.cancel(false)
    }

  }

}
