
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

  // A one-to-many mapping from a player to all of the players who
  // voted for them. Players who received no votes are not included.
  private def reverseMapping: Map[Id[User], List[Id[User]]] =
    mapping.toList.groupMap { (_, target) => target } { (voter, _) => voter }

  def displayText(userMapping: UserMapping): String = {
    val votals = reverseMapping.map { (target, voters) =>
      val voterNames = Grammar.conjunctionList(voters.map { userMapping.nameOf(_) })
      s"${userMapping.nameOf(target)} (${voters.length}) - ${voterNames}"
    }.mkString("\n")
    s"Final day votals:\n${votals}"
  }

  def htmlText(userMapping: UserMapping)(using HtmlFragment): Unit = {
    li.attr("class" := "player-votals") {
      t("Final day votals:")
      ul.attr("class" := "player-votals-list") {
        reverseMapping.foreach { (target, voters) =>
          val voterNames = Grammar.conjunctionList(voters.map { userMapping.nameOf(_) })
          li.attr("class" := "player-votals-item") {
            t(userMapping.nameOf(target))
            t(s" (${voters.length}) - ")
            t(voterNames)
          }
        }
      }
    }
  }

}
