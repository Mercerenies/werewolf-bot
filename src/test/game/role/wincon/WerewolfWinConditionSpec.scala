
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

  it should "count a town victory if a werewolf dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(3),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a werewolf victory if an ordinary paranormal investigator dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, ParanormalInvestigator),
      deadPlayers = List(4),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a werewolf victory, together with a paranormal investigator werewolf, if no one dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, ParanormalInvestigator),
      deadPlayers = List(),
    )
    endgame.board(id(4)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Werewolf.createInstance(SampleUserMapping(5), Some(id(4))))

    WinCondition.determineWinners(endgame) should equal (List(2, 3, 4)) (after being unordered)

  }

  it should "count a town victory if a paranormal investigator who copied a werewolf dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Werewolf, ParanormalInvestigator),
      deadPlayers = List(4),
    )
    endgame.board(id(4)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Werewolf.createInstance(SampleUserMapping(5), Some(id(4))))

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

}
