
package com.mercerenies.werewolf
package logging

import org.apache.logging.log4j.Logger

import scalaz.*
import Scalaz.*

import scala.util.{Try, Failure, Success}

object Logs {

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

  extension[A] (self: Try[A])
    def logErrors(logger: Logger): Option[A] =
      self match {
        case Success(a) => {
          Some(a)
        }
        case Failure(e) => {
          logger.catching(e)
          None
        }
      }

}
