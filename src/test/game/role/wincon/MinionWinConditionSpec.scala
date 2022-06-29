
package com.mercerenies.werewolf
package game
package role
package wincon

import id.{Id, UserMapping}
import board.{Board, Endgame}
import board.EndgameTestUtil.*
import role.{Role, RoleInstance}
import IterableNormalizations.*

import org.javacord.api.entity.user.User

class MinionWinConditionSpec extends UnitSpec {

  "The minion win condition" should "count a victory if only villagers die" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Minion),
      deadPlayers = List(0),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3, 4)) (after being unordered)

  }

  it should "count a minion victory if no one dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Minion),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3, 4)) (after being unordered)

  }

  it should "count a minion victory if the minion dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Minion),
      deadPlayers = List(4),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3, 4)) (after being unordered)

  }

  it should "count a minion + paranormal investigator victory if the minion dies and a paranormal investigator viewed a minion" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Minion, ParanormalInvestigator),
      deadPlayers = List(4),
    )
    endgame.board(id(5)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Minion.createInstance(SampleUserMapping(6), Some(id(5))))

    WinCondition.determineWinners(endgame) should equal (List(2, 3, 4, 5)) (after being unordered)

  }

  it should "count a minion loss if a werewolf dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Minion),
      deadPlayers = List(2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a minion loss if a werewolf dies in a split vote" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Minion),
      deadPlayers = List(0, 2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a minion victory if no one dies and there are no werewolves" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Minion, Villager),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 2, 3)) (after being unordered)

  }

  it should "count as no victory if a villager dies and there are no werewolves" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Minion, Villager),
      deadPlayers = List(1),
    )

    WinCondition.determineWinners(endgame) should equal (List()) (after being unordered)

  }

  it should "count as no victory if the minion dies and there are no werewolves" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Minion, Villager),
      deadPlayers = List(2),
    )

    WinCondition.determineWinners(endgame) should equal (List()) (after being unordered)

  }

}
