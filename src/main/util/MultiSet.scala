
package com.mercerenies.werewolf
package util

import collection.immutable.Map
import math.Ordering

final class MultiSet[A] private(
  private val counts: Map[A, Int],
) extends Iterable[A] {

  def iterator: Iterator[A] =
    counts.iterator.flatMap { (a, n) => List.fill(n) { a } }

  override def equals(that: Any): Boolean = that match {
    case that: MultiSet[?] => this.counts == that.counts
    case _ => false
  }

  override def hashCode: Int =
    ("MultiSet", counts).hashCode

  def sorted(using Ordering[A]): List[A] =
    toList.sorted

  def sortBy[B](f: A => B)(using Ordering[B]): List[A] =
    toList.sortBy(f)

  def sortWith(f: (A, A) => Boolean): List[A] =
    toList.sortWith(f)

}

object MultiSet {

  def empty[A]: MultiSet[A] =
    new MultiSet(Map.empty)

  def from[A](coll: IterableOnce[A]): MultiSet[A] =
    new MultiSet(coll.toList.groupBy { k => k }.map { (k, v) => (k, v.length) })

  def apply[A](elements: A*): MultiSet[A] =
    from(elements)

}
