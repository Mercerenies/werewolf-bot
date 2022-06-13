
package com.mercerenies.werewolf
package util

import java.util.concurrent.LinkedBlockingQueue

// A thread-safe cell containing data. The data inside the cell should
// not be mutated in-place. This cell provides safe concurrent
// functions for replacing the value in the cell.
final class Cell[A](
  initialValue: A,
) {

  private var impl: A = initialValue

  private val _lock: AnyRef = new AnyRef()

  def value: A =
    _lock.synchronized {
      impl
    }

  // Returns the prior value.
  def replace(x: A): A = {
    _lock.synchronized {
      val prior = impl
      impl = x
      prior
    }
  }

  // Convenience for .replace if you don't need the prior value.
  def value_=(x: A): Unit = {
    replace(x)
  }

  // Note: modify acquires one lock, so no actions can happen between
  // the get and subsequent set of the data.
  def modify(fn: (A) => A): Unit = {
    _lock.synchronized {
      val prior = impl
      impl = fn(prior)
    }
  }

  // Lock access to this cell for the duration of evaluation of the
  // by-name argument.
  def lock[B](fn: => B): B =
    _lock.synchronized { fn }

}
