
package com.mercerenies.werewolf
package game
package record

import id.UserMapping

import scala.collection.immutable.Vector

class RecordedGameHistory private(val impl: Vector[GameRecord]) {

  def +(record: GameRecord): RecordedGameHistory =
    RecordedGameHistory(impl :+ record)

  def ++(records: IterableOnce[GameRecord]): RecordedGameHistory =
    RecordedGameHistory(impl ++ records)

  def toVector: Vector[GameRecord] =
    impl

  def toList: List[GameRecord] =
    impl.toList

  def map(fn: (GameRecord) => GameRecord): RecordedGameHistory =
    RecordedGameHistory(impl.map(fn))

}

object RecordedGameHistory {

  val empty: RecordedGameHistory = RecordedGameHistory(Vector())

}
