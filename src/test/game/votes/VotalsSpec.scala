
package com.mercerenies.werewolf
package game
package votes

import IterableNormalizations.*

class VotalsSpec extends UnitSpec {

  private val sampleVotes: Votals[Int] =
    Votals(Map(1 -> 1, 2 -> 1, 3 -> 2, 4 -> 1, 5 -> 2, 6 -> 3))

  private def votals(x: List[Int]): Votals[Int] =
    Votals(x.zipWithIndex.map { (target, index) => (index + 1, target) }.toMap)

  "The Votals class" should "allow access to the votes via .apply" in {
    sampleVotes(1) should be (1)
    sampleVotes(2) should be (1)
    sampleVotes(3) should be (2)
    sampleVotes(4) should be (1)
    sampleVotes(5) should be (2)
    sampleVotes(6) should be (3)
    a [NoSuchElementException] should be thrownBy sampleVotes(7)
  }

  it should "allow access to the votes via .get" in {
    sampleVotes.get(1) should be (Some(1))
    sampleVotes.get(2) should be (Some(1))
    sampleVotes.get(3) should be (Some(2))
    sampleVotes.get(4) should be (Some(1))
    sampleVotes.get(5) should be (Some(2))
    sampleVotes.get(6) should be (Some(3))
    sampleVotes.get(7) should be (None)
  }

  it should "provide access to the players list" in {
    sampleVotes.players.toSet should be (Set(1, 2, 3, 4, 5, 6))
  }

  it should "provide access to the reverse mapping from players to current votes on them" in {
    val reverse = sampleVotes.reverseMapping
    reverse.keys.toSet should be (Set(1, 2, 3)) // Excludes players with no votes
    reverse(1) should equal (List(1, 2, 4)) (after being unordered)
    reverse(2) should equal (List(3, 5)) (after being unordered)
    reverse(3) should equal (List(6)) (after being unordered)
  }

  it should "provide access to the counts from players to current vote tally" in {
    sampleVotes.counts should be (Map(1 -> 3, 2 -> 2, 3 -> 1, 4 -> 0, 5 -> 0, 6 -> 0))
  }

  it should "provide access to the multiset of all votes" in {
    sampleVotes.allVotes.toList should equal (List(1, 1, 1, 2, 2, 3)) (after being unordered)
  }

  it should "correctly count up the majority" in {
    val voteList = votals(List(1, 1, 2, 1, 2, 3))
    voteList.majority should equal (List(1)) (after being unordered)
  }

  it should "allow for ties in the majority" in {
    val voteList = votals(List(1, 1, 2, 1, 2, 2, 3))
    voteList.majority should equal (List(1, 2)) (after being unordered)
  }

  it should "allow for ties in the majority, even if everyone would be majority" in {
    val voteList = votals(List(1, 1, 2, 1, 2, 2, 3, 3, 3))
    voteList.majority should equal (List(1, 2, 3)) (after being unordered)
  }

  it should "not count as a majority if each entity has only one vote" in {
    val voteList = votals(List(1, 2, 3))
    voteList.majority should equal (List()) (after being unordered)
  }

  it should "not count as a majority if there is only one entity and it only has one vote" in {
    val voteList = votals(List(1))
    voteList.majority should equal (List()) (after being unordered)
  }

  it should "not count as a majority if there are no votes" in {
    val voteList = votals(List())
    voteList.majority should equal (List()) (after being unordered)
  }

}
