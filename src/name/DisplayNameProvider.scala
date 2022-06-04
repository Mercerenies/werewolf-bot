
package com.mercerenies.werewolf
package name

import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User

class DisplayNameProvider(private val server: Server) extends NameProvider {
  override def getNameOf(user: User): String =
    user.getDisplayName(server)
}
