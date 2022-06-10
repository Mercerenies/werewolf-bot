
package com.mercerenies.werewolf
package name

import org.javacord.api.entity.server.Server
import org.javacord.api.entity.user.User

trait NameProvider {
  def getNameOf(user: User): String
}

object NameProvider {

  def fromOptionServer(opt: Option[Server]): NameProvider =
    opt.fold(BaseNameProvider) { DisplayNameProvider(_) }

}
