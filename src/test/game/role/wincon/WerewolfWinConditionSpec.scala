
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

class WerewolfWinConditionSpec extends UnitSpec {

  "The werewolf win condition" should "count a werewolf victory if only villagers die" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(0),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a werewolf victory if no one dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

}
