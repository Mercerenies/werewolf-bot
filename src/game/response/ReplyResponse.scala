
package com.mercerenies.werewolf
package game
package response

import org.javacord.api.DiscordApi
import org.javacord.api.entity.message.Message

import scala.concurrent.{Future, ExecutionContext}
import scala.jdk.FutureConverters.*

import scalaz.*
import Scalaz.*

open class ReplyResponse(val replyText: String) extends MessageResponse {

  override def respondTo(api: DiscordApi, message: Message)(using ExecutionContext): Future[Unit] =
    message.reply(replyText).asScala.void

}
