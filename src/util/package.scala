
package com.mercerenies.werewolf
package util

import id.Id

import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

import scala.jdk.CollectionConverters.*
import scala.util.Random
import scala.collection.mutable.HashSet

// Various convenience helpers that had nowhere better to go

def mentions(message: Message, userId: Id[User]): Boolean =
  message.getMentionedUsers.asScala.exists { _.getId == userId.toLong }

// If the two lists are not of the same length, the longer one will
// have some elements unassigned.
def randomlyAssign[A, B](lhs: Iterable[A], rhs: Iterable[B]): Iterable[(A, B)] = {
  val shuffled = Random.shuffle(rhs)
  lhs zip shuffled
}

// Finds the first duplicated element in the iterable. If there is
// none, then returns None.
def findDuplicate[A](data: IterableOnce[A]): Option[A] = {
  val collection: HashSet[A] = HashSet()
  for (term <- data.iterator) {
    if (collection.contains(term)) {
      return Some(term)
    }
    collection += term
  }
  None
}

def pluralize(number: Long, text: String): String =
  if (number == 1) {
    s"${number} ${text}"
  } else {
    s"${number} ${text}s"
  }
