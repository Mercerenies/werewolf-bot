
package com.mercerenies.werewolf
package name

import org.javacord.api.entity.user.User

object DiscriminatedNameProvider extends NameProvider {
  override def getNameOf(user: User): String =
    user.getDiscriminatedName
}
