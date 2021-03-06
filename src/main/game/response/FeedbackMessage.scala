
package com.mercerenies.werewolf
package game
package response

import org.javacord.api.DiscordApi
import org.javacord.api.entity.message.Messageable

import scalaz.*
import Scalaz.*

import scala.concurrent.{Future, ExecutionContext}
import scala.jdk.FutureConverters.*

opaque type FeedbackMessage = List[String]

object FeedbackMessage {

  def apply(xs: String*): FeedbackMessage =
    xs.toList

  def messages(xs: List[String]): FeedbackMessage =
    xs

  val none: FeedbackMessage =
    Nil

  extension(self: FeedbackMessage)

    // Concatenate with newlines to produce a result string. Mainly
    // used for testing purposes.
    def mkString: String =
      self.mkString("\n")

    def toList: List[String] =
      self

    def ++(other: FeedbackMessage): FeedbackMessage =
      self ++ other

    def sendTo(target: Messageable)(using ExecutionContext): Future[Unit] =
      self.traverse { target.sendMessage(_).asScala }.void

}
