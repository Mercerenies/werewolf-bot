
package com.mercerenies.werewolf
package state

import util.Cell

import scala.concurrent.{Future, ExecutionContext}

// A lazy value B which requires an input of type A to compute. Caches
// the first result and never runs the function again after that
// point.
class LazyValue[A, B](
  private val mapping: (A) => (ExecutionContext) ?=> Future[B]
) {

  private val storedValue: Cell[Option[B]] = Cell(None)

  def valueOption: Option[B] =
    storedValue.value

  def getValue(input: A)(using ExecutionContext): Future[B] =
    valueOption match {
      case Some(x) => Future.successful(x)
      case None => {
        for {
          result <- mapping(input)
        } yield {
          storedValue.value = Some(result)
          result
        }
      }
    }

}
