
package com.mercerenies.werewolf
package util

import java.util.concurrent.LinkedBlockingQueue

// A thread-safe cell containing data. The data inside the cell should
// not be mutated in-place. This cell provides safe concurrent
// functions for replacing the value in the cell.
final class Cell[A](
  initialValue: A,
) {

  private val impl = LinkedBlockingQueue[A](1)

  impl.put(initialValue)

  def value: A =
    impl.peek.nn

  // Returns the prior value.
  def replace(x: A): A = {
    val prior = impl.remove()
    impl.add(x)
    prior
  }

  // Convenience for .replace if you don't need the prior value.
  def value_=(x: A): Unit = {
    replace(x)
  }

  def modify(fn: (A) => A): Unit = {
    val prior = impl.remove()
    impl.add(fn(prior))
  }

}
