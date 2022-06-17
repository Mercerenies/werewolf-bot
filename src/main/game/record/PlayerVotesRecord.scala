
package com.mercerenies.werewolf
package game
package record

import id.{UserMapping, Id}
import board.{TablePosition, Position}
import board.snapshot.BoardSnapshot
import util.Grammar
import util.html.{HtmlFragment, HtmlBuilder}
import parser.assignment.{NamedUser, NamedPosition}

import org.javacord.api.entity.user.User

// A record which shows who voted for whom. The map shall be a
// one-to-one mapping from voters to targets. A player whose vote was
// chosen randomly will have the randomly-selected vote shown.
class PlayerVotesRecord(val mapping: Map[Id[User], Id[User]]) extends GameRecord {
  import HtmlBuilder.*

  def displayText(userMapping: UserMapping): String = {
    beginPlaintext {
      htmlText(userMapping)
    }
  }

  def htmlText(userMapping: UserMapping)(using HtmlFragment): Unit = {
    val reverseMapping: Map[Id[User], List[Id[User]]] =
      mapping.toList.groupMap { (_, target) => target } { (voter, _) => voter }
    li {
      t("Final day votals:")
      ul {
        reverseMapping.foreach { (target, voters) =>
          val voterNames = Grammar.conjunctionList(voters.map { userMapping.nameOf(_) })
          li {
            t(userMapping.nameOf(target))
            t(s" (${voters.length}) - ")
            t(voterNames)
          }
        }
      }
    }
  }

}
