
package com.mercerenies.werewolf
package game
package record

import id.UserMapping
import board.{TablePosition, Position}
import board.snapshot.BoardSnapshot
import util.html.{HtmlFragment, HtmlBuilder}
import parser.assignment.{NamedUser, NamedPosition}

// A record which shows the state of the board at a particular moment.
class SnapshotRecord(val snapshot: BoardSnapshot) extends GameRecord {

  // TODO Alpha wolf table position

  def displayText(userMapping: UserMapping): String = {
    val tableLines = (
      "Left: " + snapshot(TablePosition.Left) + "\n" +
      "Middle: " + snapshot(TablePosition.Middle) + "\n" +
      "Right: " + snapshot(TablePosition.Right)
    )
    val playerLines = snapshot.playerList.map { id =>
      s"${userMapping.nameOf(id)} - ${snapshot(id).name}"
    }.mkString("\n")
    s"Board state at this moment:\n${tableLines}\n${playerLines}"
  }

  def htmlText(userMapping: UserMapping)(using HtmlFragment): Unit = {

    val positions =
      TablePosition.all.map { NamedPosition.Table(_) } ++
        snapshot.playerList.map { id => NamedPosition.Player(NamedUser(id, userMapping.nameOf(id), None)) }
    val roles = positions.map { p => snapshot(p.toPosition) }

    import HtmlBuilder.*
    li {
      t("The state of the board is now:")
      div {
        table {
          // Header row (role cards)
          tr {
            roles.foreach { role =>
              td { t(role.name) }
            }
          }
          // Footer row (position / player names)
          tr {
            positions.foreach { p =>
              td { t(p.name) }
            }
          }
        }
      }
    }

  }

}
