
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
    val playerLines = snapshot.playerOrder.map { id =>
      s"${userMapping.nameOf(id)} - ${snapshot(id).name}"
    }.mkString("\n")
    s"Board state at this moment:\n${tableLines}\n${playerLines}"
  }

  def htmlText(userMapping: UserMapping)(using HtmlFragment): Unit = {

    val positions =
      TablePosition.all.map { NamedPosition.Table(_) } ++
        snapshot.playerOrder.map { id => NamedPosition.Player(NamedUser(id, userMapping.nameOf(id), None)) }
    val roles = positions.map { p => snapshot(p.toPosition) }

    import HtmlBuilder.*
    li.attr("class" := "snapshot-record") {
      t("The state of the board is now:")
      div.attr("class" := "snapshot-record-div") {
        table.attr("class" := "snapshot-record-table") {
          // Header row (role cards)
          tr.attr("class" := "snapshot-record-role-row") {
            roles.foreach { role =>
              td.attr("class" := "snapshot-record-role") { t(role.name) }
            }
          }
          // Footer row (position / player names)
          tr.attr("class" := "snapshot-record-player-row") {
            positions.foreach { p =>
              td.attr("class" := "snapshot-record-player") { t(p.name) }
            }
          }
        }
      }
    }

  }

}
