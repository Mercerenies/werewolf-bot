
package com.mercerenies.werewolf
package game
package role
package wincon

import id.{Id, UserMapping}
import board.{Board, Endgame}
import board.EndgameTestUtil.*
import role.Role
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

  it should "count a town + unturned Cursed victory if no one dies and there are no werewolves" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Villager, Villager, Cursed),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 2, 3, 4)) (after being unordered)

  }

  it should "count a town + unturned Cursed victory if a werewolf dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Villager, Villager, Cursed, Werewolf),
      deadPlayers = List(5),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 2, 3, 4)) (after being unordered)

  }

  it should "count a town victory if a werewolf dies, in a game with a turned Cursed" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Villager, Villager, Cursed, Werewolf),
      deadPlayers = List(5),
    )
    endgame.board(Id.fromLong(4)).asInstanceOf[Cursed.Instance].turnToWerewolf()

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 2, 3)) (after being unordered)

  }

  it should "count a town victory if a turned Cursed dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Villager, Villager, Cursed, Werewolf),
      deadPlayers = List(4),
    )
    endgame.board(Id.fromLong(4)).asInstanceOf[Cursed.Instance].turnToWerewolf()

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 2, 3)) (after being unordered)

  }

}
