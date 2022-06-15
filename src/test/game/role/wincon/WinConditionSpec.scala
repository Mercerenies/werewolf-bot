
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

class WinConditionSpec extends UnitSpec {

  // A mock role whose win condition shall never be accessed.
  private def inaccessibleRole(): Role = {
    val mockRole: Role = mock
    val mockRoleInstance: RoleInstance = mock

    when(mockRole.createInstance(any(classOf[UserMapping]))).thenReturn(mockRoleInstance)
    when(mockRoleInstance.winCondition).thenThrow(new RuntimeException("WinConditionSpec.inaccessibleRole"))

    mockRole
  }

  private def constantOutcomeRole(precedence: Int, outcome: Outcome): Role = {
    val mockRole: Role = mock
    val mockRoleInstance: RoleInstance = mock
    val mockWinCondition: WinCondition = mock

    when(mockRole.createInstance(any(classOf[UserMapping]))).thenReturn(mockRoleInstance)
    when(mockRoleInstance.winCondition).thenReturn(mockWinCondition)
    when(mockWinCondition.precedence).thenReturn(precedence)
    when(mockWinCondition.determineOutcome(any(classOf[Endgame]), anyId())).thenReturn(outcome)

    mockRole
  }

  "WinCondition.determineWinners" should "allow multiple soft wins to be shared among players" in {

    val p0 = constantOutcomeRole(10, Outcome.SoftWin)
    val p1 = constantOutcomeRole(10, Outcome.SoftWin)
    val p2 = constantOutcomeRole(0, Outcome.SoftWin)

    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(p0, p1, p2),
      deadPlayers = List(),
    )

    // All players share the win.
    WinCondition.determineWinners(endgame) should equal (List(0, 1, 2)) (after being unordered)

  }

  it should "allow a hard win to block lower-precedence wins" in {

    val p0 = constantOutcomeRole(30, Outcome.SoftWin)
    val p1 = constantOutcomeRole(20, Outcome.HardWin)
    val p2 = constantOutcomeRole(10, Outcome.HardWin)
    val p3 = constantOutcomeRole(0, Outcome.SoftWin)

    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(p0, p1, p2, p3),
      deadPlayers = List(),
    )

    // Only the high-precedence soft and high-precedence hard can win.
    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "allow a hard win to block lower-precedence wins while leaving losers alone" in {

    val p0 = constantOutcomeRole(50, Outcome.SoftLoss)
    val p1 = constantOutcomeRole(40, Outcome.SoftWin)
    val p2 = constantOutcomeRole(30, Outcome.HardWin)
    val p3 = constantOutcomeRole(20, Outcome.HardWin)
    val p4 = constantOutcomeRole(10, Outcome.SoftWin)
    val p5 = constantOutcomeRole(10, Outcome.SoftLoss)

    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(p0, p1, p2, p3, p4, p5),
      deadPlayers = List(),
    )

    // Only the high-precedence soft and high-precedence hard can win.
    WinCondition.determineWinners(endgame) should equal (List(1, 2)) (after being unordered)

  }

  it should "allow a hard loss to block lower-precedence wins while leaving losers alone" in {

    val p0 = constantOutcomeRole(50, Outcome.SoftLoss)
    val p1 = constantOutcomeRole(40, Outcome.SoftWin)
    val p2 = constantOutcomeRole(30, Outcome.HardLoss)
    val p3 = constantOutcomeRole(20, Outcome.HardWin)
    val p4 = constantOutcomeRole(10, Outcome.SoftWin)
    val p5 = constantOutcomeRole(10, Outcome.SoftLoss)

    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(p0, p1, p2, p3, p4, p5),
      deadPlayers = List(),
    )

    // Only the high-precedence soft wins.
    WinCondition.determineWinners(endgame) should equal (List(1)) (after being unordered)

  }

  it should "allow hard winners to share the victory with each other at the same precedence" in {

    val p0 = constantOutcomeRole(20, Outcome.SoftLoss)
    val p1 = constantOutcomeRole(20, Outcome.SoftWin)
    val p2 = constantOutcomeRole(20, Outcome.HardWin)
    val p3 = constantOutcomeRole(20, Outcome.HardWin)
    val p4 = constantOutcomeRole(10, Outcome.SoftWin)
    val p5 = constantOutcomeRole(0, Outcome.SoftLoss)

    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(p0, p1, p2, p3, p4, p5),
      deadPlayers = List(),
    )

    // The soft and hard winners at 20 should take the win.
    WinCondition.determineWinners(endgame) should equal (List(1, 2, 3)) (after being unordered)

  }

  it should "allow hard losers to share the victory with soft winners at the same precedence" in {

    val p0 = constantOutcomeRole(20, Outcome.SoftLoss)
    val p1 = constantOutcomeRole(20, Outcome.SoftWin)
    val p2 = constantOutcomeRole(20, Outcome.HardLoss)
    val p3 = constantOutcomeRole(20, Outcome.SoftWin)
    val p4 = constantOutcomeRole(10, Outcome.SoftWin)
    val p5 = constantOutcomeRole(0, Outcome.SoftLoss)

    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(p0, p1, p2, p3, p4, p5),
      deadPlayers = List(),
    )

    // The soft winners at 20 take the win.
    WinCondition.determineWinners(endgame) should equal (List(1, 3)) (after being unordered)

  }

  it should "count a town victory if a werewolf dies" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town victory if a werewolf dies, even if a villager dies as well" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(1, 2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a town victory if a werewolf dies, even if multiple villagers die as well" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(0, 1, 2, 3),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count a werewolf victory if only villagers die" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(0),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a werewolf victory if no one dies" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count a town victory if no one dies and there are no werewolves" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Villager, Villager),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 2, 3)) (after being unordered)

  }

  it should "count as a tanner win if the tanner dies" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(4),
    )

    WinCondition.determineWinners(endgame) should equal (List(4)) (after being unordered)

  }

  it should "count as a tanner loss if no one dies" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(),
    )

    WinCondition.determineWinners(endgame) should equal (List(2, 3)) (after being unordered)

  }

  it should "count as a tanner loss if only werewolves die" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(2),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1)) (after being unordered)

  }

  it should "count as a tanner-only win if the tanner dies, even if a villager dies too" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(1, 4),
    )

    WinCondition.determineWinners(endgame) should equal (List(4)) (after being unordered)

  }

  it should "count as a tanner / town win if the tanner dies and a werewolf dies too" in {
    val endgame = createEndgame(
      left = inaccessibleRole(),
      middle = inaccessibleRole(),
      right = inaccessibleRole(),
      playerCards = List(Villager, Villager, Werewolf, Werewolf, Tanner),
      deadPlayers = List(2, 4),
    )

    WinCondition.determineWinners(endgame) should equal (List(0, 1, 4)) (after being unordered)

  }

}
