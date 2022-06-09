
package com.mercerenies.werewolf
package timer

import logging.Logging
import logging.Logs.logErrors

import org.apache.logging.log4j.Logger

import java.util.concurrent.{ScheduledExecutorService, Executors, TimeUnit}

import scala.concurrent.duration.Duration
import scala.concurrent.{Promise, Future}
import scala.util.Try

// Timer implementation that uses Scala futures and promises.
// Currently, this timer only supports one-shot tasks.
final class Timer extends Logging[Timer] {

  ///// Make it cancellable :)

  private val service: ScheduledExecutorService =
    Executors.newScheduledThreadPool(2)

  private def scheduleTaskImpl(delay: Duration, task: Runnable): Unit = {
    service.schedule(task, delay.toMillis, TimeUnit.MILLISECONDS)
  }

  // As scheduleTask, but used when you don't need a Future. Any
  // uncaught exceptions during the task will be logged and
  // suppressed.
  def scheduleTaskCast(delay: Duration, task: Runnable): Unit = {
    val runnable = ErrorLoggingRunnable(task, logger)
    scheduleTaskImpl(delay, runnable)
  }

  // Schedule a one-shot task for execution after the given delay. The
  // task will return a value in the given Future. Any errors will be
  // passed to the Future object.
  def scheduleTask[A](delay: Duration, task: () => A): Future[A] = {
    val runnable = Timer.WithPromiseRunnable(task)
    scheduleTaskImpl(delay, runnable)
    runnable.future
  }

}

object Timer {

  private final class WithPromiseRunnable[A](
    private val task: () => A,
  ) extends Runnable {

    private val promise: Promise[A] = Promise()

    def future: Future[A] = promise.future

    override def run(): Unit = {
      promise.complete(Try { task() })
    }

  }

}
