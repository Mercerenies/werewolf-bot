
package com.mercerenies.werewolf
package logging

import org.apache.logging.log4j.Logger

import scala.util.{Try, Failure, Success}

object Logs {

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
