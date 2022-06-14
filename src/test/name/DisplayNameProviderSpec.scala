
package com.mercerenies.werewolf
package name

import org.javacord.api.entity.DiscordEntity
import org.javacord.api.entity.user.User
import org.javacord.api.entity.server.Server

class DisplayNameProviderSpec extends UnitSpec {

  "DisplayNameProvider" should "get the user's display name on the given server" in {
    val mockUser: User = mock
    val mockServer: Server = mock

    when(mockUser.getDisplayName(mockServer)).thenReturn("mockUserName")

    DisplayNameProvider(mockServer).getNameOf(mockUser) should be ("mockUserName")

    verify(mockUser).getDisplayName(mockServer)

  }

}
