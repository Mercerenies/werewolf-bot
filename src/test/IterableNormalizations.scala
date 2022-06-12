
package com.mercerenies.werewolf

import org.scalactic.Uniformity

trait IterableNormalizations {

  def unordered[A]: Uniformity[Iterable[A]] =
    new Uniformity[Iterable[A]] {

      override def normalized(a: Iterable[A]): Iterable[A] =
        Set.from(a)

      override def normalizedCanHandle(a: Any): Boolean =
        a.isInstanceOf[Iterable[?]]

      override def normalizedOrSame(a: Any): Any =
        a match {
          case a: Iterable[?] => Set.from(a)
          case _ => a
        }

    }

}

// Singleton object so names can either be directly imported *or*
// mixed in.
object IterableNormalizations extends IterableNormalizations
