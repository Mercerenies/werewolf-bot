
package com.mercerenies.werewolf
package name

import org.javacord.api.entity.DiscordEntity
import org.javacord.api.entity.user.User
import org.javacord.api.entity.server.Server

class BaseNameProviderSpec extends UnitSpec {

  "BaseNameProvider" should "get the user's name" in {
    val mockUser: User = mock

    when(mockUser.getName()).thenReturn("mockUserName")

    BaseNameProvider.getNameOf(mockUser) should be ("mockUserName")

    verify(mockUser).getName()

  }

}
