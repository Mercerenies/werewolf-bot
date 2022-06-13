
package com.mercerenies.werewolf
package util

import scala.util.Random

object RandomUtil {

  extension (self: Random)

    // Sequence must be nonempty
    def sample[A](seq: Seq[A]): A =
      seq(self.nextInt(seq.length))

    def sampleOption[A](seq: Seq[A]): Option[A] =
      if (seq.isEmpty) {
        None
      } else {
        Some(sample(seq))
      }

}
