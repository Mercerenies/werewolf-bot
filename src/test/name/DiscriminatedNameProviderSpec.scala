
package com.mercerenies.werewolf
package name

import org.javacord.api.entity.DiscordEntity
import org.javacord.api.entity.user.User
import org.javacord.api.entity.server.Server

class DiscriminatedNameProviderSpec extends UnitSpec {

  "DiscriminatedNameProvider" should "get the user's discriminated name" in {
    val mockUser: User = mock

    when(mockUser.getDiscriminatedName()).thenReturn("mockUserName")

    DiscriminatedNameProvider.getNameOf(mockUser) should be ("mockUserName")

    verify(mockUser).getDiscriminatedName()

  }

}
