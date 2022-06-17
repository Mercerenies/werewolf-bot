
package com.mercerenies.werewolf
package id

import org.javacord.api.entity.DiscordEntity
import org.javacord.api.entity.channel.TextChannel

class IdSpec extends UnitSpec {

  "The Id type" should "be isomorphic to a Long" in {

    val id = Id.fromLong[MockDiscordEntity](8765L)
    id.toLong should be (8765L)

    val id2 = Id.fromLong[MockDiscordEntity](8765L)
    id should be (id2)

  }

  it should "get the correct ID of a DiscordEntity object" in {
    val n = 1000L
    val entity = MockDiscordEntity(n)

    Id(entity) should be (Id.fromLong[MockDiscordEntity](n))
    Id(entity).toLong should be (n)

  }

  it should "get the ID by calling DiscordEntity.getId" in {
    val mockEntity: DiscordEntity = mock
    Id(mockEntity) // Constructing this object should call getId

    verify(mockEntity).getId()

  }

}
