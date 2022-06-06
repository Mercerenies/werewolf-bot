
package com.mercerenies.werewolf
package id

import command.CommandResponse

import org.javacord.api.DiscordApi
import org.javacord.api.entity.{DiscordEntity, Nameable}
import org.javacord.api.entity.channel.{Channel, TextChannel}
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User

import scala.jdk.OptionConverters.*
import scala.jdk.FutureConverters.*
import scala.concurrent.{Future, ExecutionContext}

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

// Extension methods on DiscordApi which get data based on Id[?] and
// return values in the EitherT[String, M, ?] monad. For some of these
// functions, by necessity, M is specialized to Future.
object Ids {

  val errorResponse: CommandResponse[Unit] =
    CommandResponse.ephemeral("Sorry, I've encountered an error. Please report this to Mercerenies :)").void

  extension(api: DiscordApi)

    def getChannel[M[_]: Monad](channelId: Id[Channel]): EitherT[String, M, Channel] =
      EitherT.fromOption(s"No such channel ${channelId}") {
        api.getChannelById(channelId.toLong).toScala.point
      }

    // Kind of an oddly specific helper, but all of our game code
    // happens inside of a named text channel, so we use this one to
    // identify channels of interest.
    def getNamedTextChannel[M[_]: Monad](channelId: Id[TextChannel & Nameable]): EitherT[String, M, TextChannel & Nameable] =
      getChannel(channelId).flatMap {
        case ch: (TextChannel & Nameable) => ch.point
        case ch => EitherT.left(s"Channel ${channelId} is not a named text channel")
      }

    def getUser(userId: Id[User]): Future[User] =
      api.getUserById(userId.toLong).asScala

    def getMessage(channel: TextChannel, messageId: Id[Message]): Future[Message] =
      api.getMessageById(messageId.toLong, channel).asScala

    def getMessage(channelId: Id[TextChannel & Nameable], messageId: Id[Message])(using ExecutionContext): EitherT[String, Future, Message] =
      getNamedTextChannel(channelId).flatMap { getMessage(_, messageId).liftM }

    def getServerFromMessage[M[_]: Monad](message: Message): EitherT[String, M, Server] =
      EitherT.fromOption(s"No server for message ${message}") {
        message.getServer.toScala.point
      }

}
