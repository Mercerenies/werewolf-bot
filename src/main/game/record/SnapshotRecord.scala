
package com.mercerenies.werewolf
package game
package record

import id.UserMapping
import board.{TablePosition, Position}
import board.snapshot.BoardSnapshot

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

}
