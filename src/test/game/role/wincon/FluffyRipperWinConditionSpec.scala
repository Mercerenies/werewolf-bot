
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

class FluffyRipperWinConditionSpec extends UnitSpec {

  import FluffyRipper.{Fluffy, Ripper}

  "The Fluffy / Ripper win condition" should "count a werewolf victory if only villagers die and Fluffy is alone" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Fluffy),
      deadPlayers = List(0),
    )

    WinCondition.determineWinners(endgame) should equal (List(2)) (after being unordered)

  }

  it should "count a werewolf victory if only villagers die and Ripper is alone" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Ripper),
      deadPlayers = List(0),
    )

    WinCondition.determineWinners(endgame) should equal (List(2)) (after being unordered)

  }

  it should "count a werewolf victory if only villagers die and Ripper is not in play" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Fluffy, Werewolf),
      deadPlayers = List(0),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a werewolf victory if only villagers die and Fluffy is not in play" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Ripper, Werewolf),
      deadPlayers = List(0),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a Fluffy victory if no one dies and Ripper is not in play" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Fluffy, Werewolf),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a Ripper victory if no one dies and Fluffy is not in play" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Ripper, Werewolf),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a town victory if a werewolf dies and Fluffy is present" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy),
      deadPlayers = List(2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town victory if a werewolf dies and Ripper is present" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Ripper),
      deadPlayers = List(2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town victory if Fluffy dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy),
      deadPlayers = List(3),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town victory if Ripper dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Ripper),
      deadPlayers = List(3),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town victory if a werewolf dies, and Fluffy and Ripper are present" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, Ripper),
      deadPlayers = List(2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town + Fluffy victory if Ripper dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, Ripper),
      deadPlayers = List(4),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 3)) (after being unordered)

  }

  it should "count a town + Ripper victory if Fluffy dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, Ripper),
      deadPlayers = List(3),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 4)) (after being unordered)

  }

  it should "count a town + Fluffy + Ripper victory if Fluffy and Ripper die" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, Ripper),
      deadPlayers = List(3, 4),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 3, 4)) (after being unordered)

  }

  it should "count a werewolf victory if an ordinary paranormal investigator dies in a game without Ripper" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, ParanormalInvestigator),
      deadPlayers = List(4),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a werewolf victory if an ordinary paranormal investigator dies in a game without Fluffy" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Ripper, ParanormalInvestigator),
      deadPlayers = List(4),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a werewolf victory, together with a paranormal investigator Fluffy, if no one dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, ParanormalInvestigator),
      deadPlayers = List(),
    )
    endgame.board(id(4)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Fluffy.createInstance(SampleUserMapping(5), Some(id(4))))

    WinCondition.determineWinners(endgame) should equal (List(2, 3, 4)) (after being unordered)

  }

  it should "count a werewolf victory, together with a paranormal investigator Ripper, if no one dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Ripper, ParanormalInvestigator),
      deadPlayers = List(),
    )
    endgame.board(id(4)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Ripper.createInstance(SampleUserMapping(5), Some(id(4))))

    WinCondition.determineWinners(endgame) should equal (List(2, 3, 4)) (after being unordered)

  }

  it should "count a town + Ripper victory if a paranormal investigator who copied Fluffy dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, Ripper, ParanormalInvestigator),
      deadPlayers = List(5),
    )
    endgame.board(id(5)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Fluffy.createInstance(SampleUserMapping(5), Some(id(5))))

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 4)) (after being unordered)

  }

  it should "count a town + Fluffy victory if a paranormal investigator who copied Ripper dies" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, Ripper, ParanormalInvestigator),
      deadPlayers = List(5),
    )
    endgame.board(id(5)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Ripper.createInstance(SampleUserMapping(5), Some(id(5))))

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 3)) (after being unordered)

  }

  it should "count a town + Ripper + PI victory if Fluffy dies and a PI copied Ripper" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, Ripper, ParanormalInvestigator),
      deadPlayers = List(3),
    )
    endgame.board(id(5)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Ripper.createInstance(SampleUserMapping(5), Some(id(5))))

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 4, 5)) (after being unordered)

  }

  it should "count a town + Fluffy + PI victory if Fluffy dies and a PI copied Fluffy" in {
    val endgame = createEndgame(
      left = Villager,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Villager, Werewolf, Fluffy, Ripper, ParanormalInvestigator),
      deadPlayers = List(4),
    )
    endgame.board(id(5)).asInstanceOf[ParanormalInvestigator.Instance].copiedRole =
      Some(Fluffy.createInstance(SampleUserMapping(5), Some(id(5))))

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 3, 5)) (after being unordered)

  }

}
