
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

class TannerWinConditionSpec extends UnitSpec {

  "The tanner win condition" should "count as a tanner win if the tanner dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(4),
    )

    WinCondition.determineWinners(endgame) should equal (List(4)) (after being unordered)

  }

  it should "count as a tanner loss if no one dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count as a tanner loss if only werewolves die" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count as a tanner-only win if the tanner dies, even if a villager dies too" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(1, 4),
    )

    WinCondition.determineWinners(endgame) should equal (List(4)) (after being unordered)

  }

  it should "count as a tanner / town win if the tanner dies and a werewolf dies too" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(2, 4),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 4)) (after being unordered)

  }

}
