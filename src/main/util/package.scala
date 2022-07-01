
package com.mercerenies.werewolf
package util

import id.Id

import org.javacord.api.entity.message.Message
import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import scala.jdk.CollectionConverters.*
import scala.util.Random
import scala.collection.mutable.HashSet

// Various convenience helpers that had nowhere better to go

def mentions(message: Message, user: User): Boolean =
  // Direct mentions
  message.getMentionedUsers.asScala.exists { _.getId == user.getId } ||
    // or Role mentions
    message.getMentionedRoles.asScala.exists { _.hasUser(user) }

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

def deleteFirst[A](list: List[A], element: A): List[A] =
  list match {
    case Nil => Nil
    case x :: xs if x == element => xs
    case x :: xs => x :: deleteFirst(xs, element)
  }

def foldM[A, B, M[_]: Monad](list: List[B], acc: A)(op: (A, B) => M[A]): M[A] =
  list match {
    case Nil => acc.point
    case (x :: xs) => op(acc, x) >>= { newAcc => foldM(xs, newAcc)(op) }
  }

// Construct a translation table from two strings of equal length.
def tr(a: String, b: String): Map[Char, Char] = {
  if (a.length != b.length) {
    throw new IllegalArgumentException("Strings of non-equal length")
  }
  (a zip b).toMap
}

def merge[K, V](m1: Map[K, V], m2: Map[K, V])(merge: (V, V) => V): Map[K, V] =
  m2.foldLeft(m1) { (m, kv) =>
    val (k, v) = kv
    m.get(k) match {
      case None => m + ((k, v))
      case Some(v0) => m + ((k, merge(v0, v)))
    }
  }
