
package com.mercerenies.werewolf
package game
package night

import response.{MessageResponse, NoResponse}
import util.TextDecorator.*
import board.Board

object NoInputNightMessageHandler extends NightMessageHandler {

  def initialNightMessage: String =
    "I require " + bold("no input") + " from you tonight."

  def midnightReminder: Option[String] =
    None

  def onDirectMessage(board: Board, messageContents: String): MessageResponse =
    NoResponse

}
