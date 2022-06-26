
package com.mercerenies.werewolf
package util

import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User
import org.javacord.api.entity.permission.Role

import scala.jdk.CollectionConverters.*

class UtilSpec extends UnitSpec {

  "The util.mentions function" should "detect pings against the given user" in {

    val user1: User = mock
    val user2: User = mock

    val message1: Message = mock
    val message2: Message = mock
    val message3: Message = mock
    val message4: Message = mock

    when(user1.getId).thenReturn(10001L)
    when(user2.getId).thenReturn(10002L)

    when(message1.getMentionedUsers).thenReturn(List().asJava)
    when(message2.getMentionedUsers).thenReturn(List(user1).asJava)
    when(message3.getMentionedUsers).thenReturn(List(user2).asJava)
    when(message4.getMentionedUsers).thenReturn(List(user1, user2).asJava)
    when(message1.getMentionedRoles).thenReturn(List().asJava)
    when(message2.getMentionedRoles).thenReturn(List().asJava)
    when(message3.getMentionedRoles).thenReturn(List().asJava)
    when(message4.getMentionedRoles).thenReturn(List().asJava)

    mentions(message1, user1) should be (false)
    mentions(message2, user1) should be (true)
    mentions(message3, user1) should be (false)
    mentions(message4, user1) should be (true)
    mentions(message1, user2) should be (false)
    mentions(message2, user2) should be (false)
    mentions(message3, user2) should be (true)
    mentions(message4, user2) should be (true)

  }

  it should "detect pings against a user with the given role" in {

    val user1: User = mock
    val user2: User = mock
    val user3: User = mock

    val message1: Message = mock
    val message2: Message = mock
    val message3: Message = mock
    val message4: Message = mock

    val allUsersRole: Role = mock
    val noUsersRole: Role = mock
    val firstTwoUsersRole: Role = mock

    when(user1.getId).thenReturn(10001L)
    when(user2.getId).thenReturn(10002L)
    when(user3.getId).thenReturn(10003L)

    when(allUsersRole.hasUser(any[User]())).thenReturn(true)
    when(noUsersRole.hasUser(any[User]())).thenReturn(false)
    when(firstTwoUsersRole.hasUser(any[User]())).thenReturn(true)
    when(firstTwoUsersRole.hasUser(user3)).thenReturn(false)

    when(message1.getMentionedUsers).thenReturn(List(user3).asJava)
    when(message2.getMentionedUsers).thenReturn(List().asJava)
    when(message3.getMentionedUsers).thenReturn(List().asJava)
    when(message4.getMentionedUsers).thenReturn(List().asJava)
    when(message1.getMentionedRoles).thenReturn(List(firstTwoUsersRole).asJava)
    when(message2.getMentionedRoles).thenReturn(List(allUsersRole).asJava)
    when(message3.getMentionedRoles).thenReturn(List(noUsersRole).asJava)
    when(message4.getMentionedRoles).thenReturn(List(firstTwoUsersRole).asJava)

    mentions(message1, user1) should be (true)
    mentions(message2, user1) should be (true)
    mentions(message3, user1) should be (false)
    mentions(message4, user1) should be (true)
    mentions(message1, user2) should be (true)
    mentions(message2, user2) should be (true)
    mentions(message3, user2) should be (false)
    mentions(message4, user2) should be (true)
    mentions(message1, user3) should be (true)
    mentions(message2, user3) should be (true)
    mentions(message3, user3) should be (false)
    mentions(message4, user3) should be (false)

  }

  // TODO The rest of this

  "The util.tr function" should "create a mapping from a pair of strings" in {
    tr("", "") should be (Map.empty)
    tr("a", "1") should be (Map('a' -> '1'))
    tr("abc", "123") should be (Map('a' -> '1', 'b' -> '2', 'c' -> '3'))
  }

  it should "take the later key in case of duplicates" in {
    tr("abca", "1234") should be (Map('a' -> '4', 'b' -> '2', 'c' -> '3'))
  }

  it should "throw an exception if given strings of non-equal length" in {
    an [IllegalArgumentException] should be thrownBy tr("abca", "d")
    an [IllegalArgumentException] should be thrownBy tr("ab", "abcde")
  }

}
