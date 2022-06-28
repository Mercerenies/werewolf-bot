
package com.mercerenies.werewolf
package game

import board.BoardTestUtil
import id.Id
import record.{RecordedGameHistory, ActionPerformedRecord}
import votes.Votals

import org.javacord.api.entity.user.User

// Helpers for unit testing classes which simulate runs of the night
// phase.
abstract class GameplayUnitSpec extends UnitSpec {

  def id(x: Long): Id[User] =
    Id.fromLong(x)

  def id(x: Int): Id[User] =
    Id.fromLong(x)

  // For many (but not all) of these tests, we simply want to check
  // that the ActionPerformedRecord events that were recorded look
  // correct. The other events (primarily SnapshotRecord) are not
  // relevant to these tests. This function filters a
  // RecordedGameHistory into a list of only the relevant events.
  def filterRecords(rec: RecordedGameHistory): List[ActionPerformedRecord] =
    rec.toList.flatMap {
      case r: ActionPerformedRecord => Some(r)
      case _ => None
    }

  def votals(args: (Int, Int)*): Votals[Id[User]] =
    Votals(args.map { (x, y) => (id(x), id(y)) }.toMap)

  export BoardTestUtil.{createBoard, SampleUserMapping}
  export TestGameRunner.{playGame, mockName}
  export TestVotesRunner.runVotes

}
