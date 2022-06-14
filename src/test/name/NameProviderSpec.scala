
package com.mercerenies.werewolf
package name

import org.javacord.api.entity.DiscordEntity
import org.javacord.api.entity.user.User
import org.javacord.api.entity.server.Server

class NameProviderSpec extends UnitSpec {

  "NameProvider.fromOptionServer" should "get the user's name if given no server" in {
    val mockUser: User = mock

    when(mockUser.getName()).thenReturn("mockUserName")

    val nameProvider = NameProvider.fromOptionServer(None)

    nameProvider.getNameOf(mockUser) should be ("mockUserName")

    verify(mockUser, never()).getDisplayName(any(classOf[Server]))
    verify(mockUser).getName()

  }

  it should "get the user's display name if given a server" in {
    val mockUser: User = mock
    val mockServer: Server = mock

    when(mockUser.getDisplayName(mockServer)).thenReturn("mockServerUserName")

    val nameProvider = NameProvider.fromOptionServer(Some(mockServer))

    nameProvider.getNameOf(mockUser) should be ("mockServerUserName")

    verify(mockUser).getDisplayName(mockServer)
    verify(mockUser, never()).getName()

  }

}
