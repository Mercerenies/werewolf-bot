
package com.mercerenies.werewolf
package command

import org.javacord.api.interaction.{SlashCommandInteractionOption, SlashCommandOptionType}
import org.javacord.api.listener.interaction.SlashCommandCreateListener
import org.javacord.api.event.interaction.SlashCommandCreateEvent
import org.javacord.api.entity.Mentionable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.channel.ServerChannel
import org.javacord.api.DiscordApi

import scala.concurrent.{Future, ExecutionContext}
import scala.collection.JavaConverters.*
import scala.jdk.FutureConverters.*
import scala.jdk.OptionConverters.*
import scala.language.implicitConversions

sealed trait ArgumentType[R] {

  def optionType: SlashCommandOptionType

  def getArgument(opt: SlashCommandInteractionOption): R

}

object ArgumentType {

  private def unwrap[A](argType: ArgumentType[?], opt: java.util.Optional[A]): A =
    opt.toScala match {
      case None => throw new ArgumentTypeException(argType)
      case Some(x) => x
    }

  object BooleanArg extends ArgumentType[Boolean] {
    override val optionType = SlashCommandOptionType.BOOLEAN

    def getArgument(opt: SlashCommandInteractionOption): Boolean =
      unwrap(this, opt.getBooleanValue): java.lang.Boolean

  }

  object ChannelArg extends ArgumentType[ServerChannel] {
    override val optionType = SlashCommandOptionType.CHANNEL

    def getArgument(opt: SlashCommandInteractionOption): ServerChannel =
      unwrap(this, opt.getChannelValue)

  }

  object DecimalArg extends ArgumentType[Double] {
    override val optionType = SlashCommandOptionType.DECIMAL

    def getArgument(opt: SlashCommandInteractionOption): Double =
      unwrap(this, opt.getDecimalValue): java.lang.Double

  }

  object LongArg extends ArgumentType[Long] {
    override val optionType = SlashCommandOptionType.LONG

    def getArgument(opt: SlashCommandInteractionOption): Long =
      unwrap(this, opt.getLongValue): java.lang.Long

  }

  object MentionableArg extends ArgumentType[Future[Mentionable]] {
    override val optionType = SlashCommandOptionType.MENTIONABLE

    def getArgument(opt: SlashCommandInteractionOption): Future[Mentionable] =
      unwrap(this, opt.requestMentionableValue).asScala

  }

  object RoleArg extends ArgumentType[Role] {
    override val optionType = SlashCommandOptionType.ROLE

    def getArgument(opt: SlashCommandInteractionOption): Role =
      unwrap(this, opt.getRoleValue)

  }

  object StringArg extends ArgumentType[String] {
    override val optionType = SlashCommandOptionType.STRING

    def getArgument(opt: SlashCommandInteractionOption): String =
      unwrap(this, opt.getStringValue)

  }

  object UserArg extends ArgumentType[Future[User]] {
    override val optionType = SlashCommandOptionType.USER

    def getArgument(opt: SlashCommandInteractionOption): Future[User] =
      unwrap(this, opt.requestUserValue).asScala

  }

}
