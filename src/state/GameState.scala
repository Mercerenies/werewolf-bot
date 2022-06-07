
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

  // Determines which players this GameState is interested in
  // listening to DMs from.
  //
  // Important note: This is a `val`. This value shall remain constant
  // for the duration of the GameState's existence. This may or may
  // not correspond with the actual participant list of the Werewolf
  // game (in particular, in SignUpState, this value is always empty,
  // even when there are players on the list). This is merely the list
  // of users the bot should be listening for DMs from.
  val listeningPlayerList: List[Id[User]]

  def onReactionsUpdated(mgr: GamesManager, message: Message): Unit

  def onMessageCreate(mgr: GamesManager, message: Message): Unit

  def onStartGame(mgr: GamesManager, interaction: SlashCommandInteraction): Future[CommandResponse[Unit]]

}
