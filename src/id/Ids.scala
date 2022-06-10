
package com.mercerenies.werewolf
package id

import command.CommandResponse

import org.javacord.api.DiscordApi
import org.javacord.api.entity.{DiscordEntity, Nameable}
import org.javacord.api.entity.channel.{Channel, TextChannel, ServerTextChannel}
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User

import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.concurrent.{Future, ExecutionContext}

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

// Extension methods on DiscordApi which get data based on Id[?].
// Errors that occur during this process are raised as appropriate
// DiscordIdLookupExceptions. By necessity, some of these functions
// are asynchronous. All asynchronous returns are converted to Scala
// Futures.
object Ids {

  // TODO Use me, if we catch an exception in GamesManager or any of
  // its event handlers.
  val errorResponse: CommandResponse[Unit] =
    CommandResponse.ephemeral("Sorry, I've encountered an error. Please report this to Mercerenies :)").void

  extension(api: DiscordApi)

    def getChannel(channelId: Id[Channel]): Channel =
      api.getChannelById(channelId.toLong).toScala match {
        case None => throw new DiscordIdLookupException(s"No such channel ${channelId}")
        case Some(x) => x
      }

    // Kind of an oddly specific helper, but all of our game code
    // happens inside of a named text channel, so we use this one to
    // identify channels of interest.
    def getNamedTextChannel(channelId: Id[TextChannel & Nameable]): TextChannel & Nameable =
      getChannel(channelId) match {
        case ch: (TextChannel & Nameable) => ch
        case _ => throw new DiscordIdLookupException(s"Channel ${channelId} is not a named text channel")
      }

    def getServerTextChannel(channelId: Id[ServerTextChannel]): ServerTextChannel =
      getChannel(channelId) match {
        case ch: ServerTextChannel => ch
        case _ => throw new DiscordIdLookupException(s"Channel ${channelId} is not a server text channel")
      }

    def getUser(userId: Id[User]): Future[User] =
      api.getUserById(userId.toLong).asScala

    def getMessage(channel: TextChannel, messageId: Id[Message]): Future[Message] =
      api.getMessageById(messageId.toLong, channel).asScala

    def getMessage(channelId: Id[TextChannel & Nameable], messageId: Id[Message])(using ExecutionContext): Future[Message] = {
      val channel =getNamedTextChannel(channelId)
      getMessage(channel, messageId)
    }

    def getServerFromMessage(message: Message): Server =
      message.getServer.toScala match {
        case None => throw new DiscordIdLookupException(s"No server for message ${message}")
        case Some(server) => server
      }

}
