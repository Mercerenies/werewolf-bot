
package com.mercerenies.werewolf
package logging

import org.apache.logging.log4j.Logger

import scalaz.*
import Scalaz.*

object LogEither {

  extension[E, M[_]: Monad, A] (self: EitherT[E, M, A])
    def warningToLogger(logger: Logger): M[Option[A]] =
      self.run.map {
        case -\/(e) => {
          logger.warn(e)
          None
        }
        case \/-(a) => {
          Some(a)
        }
      }

}
