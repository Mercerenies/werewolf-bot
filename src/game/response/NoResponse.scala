
package com.mercerenies.werewolf
package game
package response

import org.javacord.api.DiscordApi
import org.javacord.api.entity.message.Message

import scala.concurrent.{Future, ExecutionContext}

object NoResponse extends MessageResponse {

  def respondTo(api: DiscordApi, message: Message)(using ExecutionContext): Future[Unit] =
    Future.successful(())

}
