
package com.mercerenies.werewolf
package state

import IterableNormalizations.*

// TODO The rest of this
class VotePhaseStateSpec extends UnitSpec {

  "The VotePhaseState singleton" should "correctly count up the majority" in {
    val voteList = List(1, 1, 2, 1, 2, 3)
    VotePhaseState.getMajority(voteList) should equal (List(1)) (after being unordered)
  }

  it should "allow for ties in the majority" in {
    val voteList = List(1, 1, 2, 1, 2, 2, 3)
    VotePhaseState.getMajority(voteList) should equal (List(1, 2)) (after being unordered)
  }

  it should "allow for ties in the majority, even if everyone would be majority" in {
    val voteList = List(1, 1, 2, 1, 2, 2, 3, 3, 3)
    VotePhaseState.getMajority(voteList) should equal (List(1, 2, 3)) (after being unordered)
  }

  it should "not count as a majority if each entity has only one vote" in {
    val voteList = List(1, 2, 3)
    VotePhaseState.getMajority(voteList) should equal (List()) (after being unordered)
  }

  it should "not count as a majority if there is only one entity and it only has one vote" in {
    val voteList = List(1)
    VotePhaseState.getMajority(voteList) should equal (List()) (after being unordered)
  }

  it should "not count as a majority if there are no votes" in {
    val voteList = List()
    VotePhaseState.getMajority(voteList) should equal (List()) (after being unordered)
  }

}
