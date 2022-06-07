
package com.mercerenies.werewolf
package game
package night

import response.{MessageResponse, NoResponse}
import util.TextDecorator.*

object NoInputNightMessageHandler extends NightMessageHandler {

  def initialNightMessage: String =
    "I require " + bold("no input") + " from you tonight."

  def onDirectMessage(messageContents: String): MessageResponse =
    NoResponse

}
