
package com.mercerenies.werewolf
package game
package night

import response.MessageResponse
import board.Board

trait NightMessageHandler {

  def initialNightMessage: String

  def midnightReminder: Option[String]

  // Handle the message, potentially mutating local state to do it,
  // and then return the appropriate reply message (if any) to send
  // back to the user.
  def onDirectMessage(board: Board, messageContents: String): MessageResponse

}
