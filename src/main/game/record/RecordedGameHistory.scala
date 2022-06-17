
package com.mercerenies.werewolf
package game
package record

import scalaz.*
import Scalaz.*

import scala.collection.immutable.Vector

class RecordedGameHistory private(val impl: Vector[GameRecord]) {

  def +(record: GameRecord): RecordedGameHistory =
    RecordedGameHistory(impl :+ record)

  def ++(records: IterableOnce[GameRecord]): RecordedGameHistory =
    RecordedGameHistory(impl ++ records)

  def ++(records: RecordedGameHistory): RecordedGameHistory =
    this ++ records.impl

  def toVector: Vector[GameRecord] =
    impl

  def toList: List[GameRecord] =
    impl.toList

  def map(fn: (GameRecord) => GameRecord): RecordedGameHistory =
    RecordedGameHistory(impl.map(fn))

}

object RecordedGameHistory {

  val empty: RecordedGameHistory = RecordedGameHistory(Vector())

  given RecordedGameHistoryIsMonoid : Monoid[RecordedGameHistory] with

    def zero: RecordedGameHistory = empty

    def append(a: RecordedGameHistory, b: => RecordedGameHistory): RecordedGameHistory =
      a ++ b

}
