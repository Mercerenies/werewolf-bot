
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

class TownWinConditionSpec extends UnitSpec {

  "The town win condition" should "count a town victory if a werewolf dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town victory if a werewolf dies, even if a villager dies as well" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(1, 2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town victory if a werewolf dies, even if multiple villagers die as well" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(0, 1, 2, 3),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town victory if no one dies and there are no werewolves" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Villager, Villager),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 2, 3)) (after being unordered)

  }

  it should "count as no victory if someone dies and there are no werewolves" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Villager, Villager),
      deadPlayers = List(1),
    )

    WinCondition.determineWinners(endgame) should equal (List()) (after being unordered)

  }

}
