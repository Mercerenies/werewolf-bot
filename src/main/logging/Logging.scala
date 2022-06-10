
package com.mercerenies.werewolf
package logging

import org.apache.logging.log4j.{LogManager, Logger}

import scala.reflect.ClassTag

// There is an Apache Log4j 2 wrapper that does basically this and
// more available for Scala, but it does not seem to work in Scala 3,
// so we're doing it ourselves here. Mix this trait in with any class
// and get a free field called 'logger'.
transparent trait Logging[A](using cls: ClassTag[A]) {

  val logger: Logger = LogManager.getLogger(cls.runtimeClass)

}
