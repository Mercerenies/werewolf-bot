
package com.mercerenies.werewolf
package peano

import java.util.NoSuchElementException

sealed trait FixList[+A, N <: Num](using canonical: N) extends Seq[A] {

  override def iterator: Iterator[A] =
    FixList.FixListIterator(this)

  def get(i: Int): Option[A]

  override def apply(i: Int): A =
    get(i) match {
      case None => throw NoSuchElementException("FixList.apply")
      case Some(x) => x
    }

  override def length: Int =
    canonical.toInt

  infix def zip[B](that: FixList[B, N]): FixList[(A, B), N]

  // Some weirdness with IterableOps is going on, so I need my
  // abstract method to be a protected one with a unique name not in
  // the collection hierarchy. Then map can delegate to that.
  protected def mapImpl[B](f: (A) => B): FixList[B, N]

  override infix def map[B](f: (A) => B): FixList[B, N] =
    mapImpl(f)

}

object FixList {

  import Num.{Zero, Succ}

  private class FixListIterator[A](
    private var fixlist: FixList[A, ?],
  ) extends Iterator[A] {

    override def hasNext: Boolean =
      fixlist.isInstanceOf[Cons[?, ?]]

    override def next(): A =
      fixlist match {
        case Nil => throw new NoSuchElementException("FixListIterator.next")
        case Cons(head, tail) => {
          fixlist = tail
          head
        }
      }

  }

  object Nil extends FixList[Nothing, Zero] {

    override def get(i: Int): Option[Nothing] = None

    override infix def zip[B](that: FixList[B, Zero]): FixList[(Nothing, B), Zero] =
      Nil

    override protected def mapImpl[B](f: (Nothing) => B): FixList[B, Zero] =
      Nil

  }

  case class Cons[+A, N <: Num](
    override val head: A,
    override val tail: FixList[A, N],
  )(using N) extends FixList[A, Succ[N]] {

    override def get(i: Int): Option[A] =
      if (i == 0) {
        Some(head)
      } else {
        tail.get(i - 1)
      }

    override infix def zip[B](that: FixList[B, Succ[N]]): FixList[(A, B), Succ[N]] =
      that match {
        case Cons(head1, tail1) => {
          Cons((head, head1), tail zip tail1)
        }
      }

    override protected def mapImpl[B](f: (A) => B): FixList[B, Succ[N]] =
      Cons(f(head), tail.map(f))

  }

}
