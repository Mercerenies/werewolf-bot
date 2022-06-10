
package com.mercerenies.werewolf
package id

import org.javacord.api.event.message.CertainMessageEvent

object Messages {

  def wasSentByThisBot(event: CertainMessageEvent): Boolean =
    event.getMessage.getAuthor.getId == event.getApi.getYourself.getId

}
