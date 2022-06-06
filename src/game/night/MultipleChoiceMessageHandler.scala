
package com.mercerenies.werewolf
package game
package night

import name.NamedEntity
import response.MessageResponse

trait MultipleChoiceMessageHandler[A <: NamedEntity] {

  def choices: List[A]

  def onDirectMessage(messageContents: String): MessageResponse = ??? /////

}
