
package com.mercerenies.werewolf
package game
package record

import id.{UserMapping, Id}
import board.{TablePosition, Position}
import board.snapshot.BoardSnapshot
import util.Grammar
import util.html.{HtmlFragment, HtmlBuilder}
import parser.assignment.{NamedUser, NamedPosition}
import votes.Votals

import org.javacord.api.entity.user.User

// A record which shows who voted for whom. The map shall be a
// one-to-one mapping from voters to targets. A player whose vote was
// chosen randomly will have the randomly-selected vote shown.
class PlayerVotesRecord(val mapping: Votals[Id[User]]) extends GameRecord {
  import HtmlBuilder.*

  def displayText(userMapping: UserMapping): String = {
    val votals = mapping.reverseMapping.map { (target, voters) =>
      val voterNames = Grammar.conjunctionList(voters.map { userMapping.nameOf(_) })
      s"${userMapping.nameOf(target)} (${voters.length}) - ${voterNames}"
    }.mkString("\n")
    s"Final day votals:\n${votals}"
  }

  def htmlText(userMapping: UserMapping)(using HtmlFragment): Unit = {
    li.attr("class" := "player-votals") {
      t("Final day votals:")
      ul.attr("class" := "player-votals-list") {
        mapping.reverseMapping.foreach { (target, voters) =>
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
