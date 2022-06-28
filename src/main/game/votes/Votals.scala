
package com.mercerenies.werewolf
package game
package votes

case class Votals[A](
  // Mapping from entities to who they voted for.
  val votesMap: Map[A, A],
) {

  export votesMap.{get, apply}

  def toMap: Map[A, A] =
    votesMap

  def players: Iterable[A] =
    votesMap.keys

  // A one-to-many mapping from a player to all players who voted for
  // them. Excludes players with no votes.
  def reverseMapping: Map[A, List[A]] =
    votesMap.toList.groupMap { (_, target) => target } { (voter, _) => voter }

  // A mapping from a player to the number of votes received. Players
  // with zero votes will be present in this map and have a value of
  // 0.
  def counts: Map[A, Int] = {
    val m = reverseMapping
    players.map { k => (k, m.getOrElse(k, Nil).length) }.toMap
  }

  // Returns all votes, in no particular order. Players with multiple
  // votes will appear multiple times.
  def allVotes: Iterable[A] =
    votesMap.values

  // No guarantees on the order of the outputs.
  def majority: List[A] = {
    val grouped: Map[A, Int] = allVotes.groupBy(identity).map { (k, v) => (k, v.size) }
    val mostVotes = grouped.values.maxOption.getOrElse(0)
    if (mostVotes <= 1) {
      // No one dies if everyone has one vote
      Nil
    } else {
      grouped.filter { (_, v) => v == mostVotes }.keys.toList
    }
  }

}
