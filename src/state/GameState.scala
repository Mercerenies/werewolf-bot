
package com.mercerenies.werewolf
package state

import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.Message

// Parent trait for the states a game can be in.
trait GameState {

  def channelId: Id[TextChannel]

  def hostId: Id[User]

  def onReactionsUpdated(message: Message): Unit

}
