
package com.mercerenies.werewolf
package game
package night

import name.NamedEntity
import response.MessageResponse

trait MultipleChoiceMessageHandler[A <: NamedEntity] {

  def choices: List[A]

  // The number of choices we expect the user to input.
  def expectedNumber: Int

  // Whether the user is allowed to choose the same thing multiple
  // times.
  def repeatsAllowed: Boolean

  def onDirectMessage(messageContents: String): MessageResponse = ??? /////

}
