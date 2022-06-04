
package com.mercerenies.werewolf
package state

import id.Id
import command.CommandResponse
import manager.GamesManager

import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.interaction.SlashCommandInteraction

import scala.concurrent.Future

// Parent trait for the states a game can be in.
trait GameState {

  def channelId: Id[TextChannel]

  def hostId: Id[User]

  def onReactionsUpdated(mgr: GamesManager, message: Message): Unit

  def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]]

}
