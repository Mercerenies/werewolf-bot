
package com.mercerenies.werewolf
package util

import id.Id

import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

import scala.jdk.CollectionConverters.*

// Various convenience helpers that had nowhere better to go

def mentions(message: Message, userId: Id[User]): Boolean =
  message.getMentionedUsers.asScala.exists { _.getId == userId.toLong }
