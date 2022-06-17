
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

// A record which shows who has died.
class PlayerDeathsRecord(val deaths: List[Id[User]]) extends GameRecord {
  import HtmlBuilder.*

  def displayText(userMapping: UserMapping): String = {
    deaths.length match {
      case 0 => "No one has died."
      case 1 => s"${userMapping.nameOf(deaths(0))} has died."
      case _ => {
        val list = Grammar.conjunctionList(deaths.map { userMapping.nameOf(_) })
        s"${list} have died."
      }
    }
  }

  def htmlText(userMapping: UserMapping)(using HtmlFragment): Unit = {
    li {
      t(displayText(userMapping))
    }
  }

}
