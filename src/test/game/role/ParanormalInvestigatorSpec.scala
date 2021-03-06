
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import wincon.*
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class ParanormalInvestigatorSpec extends GameplayUnitSpec {

  "The Paranormal Investigator role" should "be included in the global role list" in {
    Role.all should contain (ParanormalInvestigator)
  }

  it should "perform no action if a PI fails to respond" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, ParanormalInvestigator, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    finalBoard(id(1)).winCondition should be (TownWinCondition)
    finalBoard(id(1)).seenAs should be (Nil)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include (mockName(0))
    feedback(id(1)).mkString should not include (mockName(1))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (1)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should not include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should not include (mockName(2))

  }

  it should "perform no action if a PI specifically opts out" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, ParanormalInvestigator, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "none", ""))
    finalBoard should be (board)

    finalBoard(id(1)).winCondition should be (TownWinCondition)
    finalBoard(id(1)).seenAs should be (Nil)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should not include (mockName(0))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (1)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should not include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should not include (mockName(2))

  }

  it should "look at the indicated card" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, ParanormalInvestigator, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", mockName(0), "", ""))
    finalBoard should be (board)

    finalBoard(id(1)).winCondition should be (TownWinCondition)
    finalBoard(id(1)).seenAs should be (Nil)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex ("(?i)villager")
    feedback(id(3)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (1)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(4)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(4)) should include regex ("(?i)villager")

  }

  it should "look at the indicated cards in order, if two are supplied" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, ParanormalInvestigator, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", mockName(0) + " " + mockName(1), "", ""))
    finalBoard should be (board)

    finalBoard(id(2)).winCondition should be (TownWinCondition)
    finalBoard(id(2)).seenAs should be (Nil)

    feedback(id(2)).mkString should include (mockName(0))
    feedback(id(2)).mkString should include regex ("(?i)villager")
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(2)).mkString should include regex ("(?i)villager")

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (2)
    filtered(0).displayText(SampleUserMapping(5)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(5)) should include regex ("(?i)villager")
    filtered(1).displayText(SampleUserMapping(5)) should include (mockName(1))
    filtered(1).displayText(SampleUserMapping(5)) should include regex ("(?i)villager")

  }

  it should "transform into a werewolf if one is observed" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, ParanormalInvestigator, Werewolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", mockName(3), "", ""))
    finalBoard should be (board)

    finalBoard(id(2)).winCondition should be (WerewolfWinCondition)
    finalBoard(id(2)).seenAs should be (List(GroupedRoleIdentity.Werewolf))

    feedback(id(2)).mkString should include (mockName(3))
    feedback(id(2)).mkString should include regex ("(?i)werewolf")

    val filtered = filterRecords(history)
    history.toList should have length (4)
    filtered should have length (3)
    filtered(2).displayText(SampleUserMapping(5)) should include (mockName(3))
    filtered(2).displayText(SampleUserMapping(5)) should include regex ("(?i)werewolf")

  }

  it should "transform into a Dream Wolf if one is observed" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, ParanormalInvestigator, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", mockName(3), "", ""))
    finalBoard should be (board)

    finalBoard(id(2)).winCondition should be (WerewolfWinCondition)
    finalBoard(id(2)).seenAs should be (List(GroupedRoleIdentity.Werewolf, GroupedRoleIdentity.DreamWolf))

    feedback(id(2)).mkString should include (mockName(3))
    feedback(id(2)).mkString should include regex ("(?i)werewolf")

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(5)) should include (mockName(3))
    filtered(0).displayText(SampleUserMapping(5)) should include regex ("(?i)dream wolf")

  }

  it should "transform into a tanner if one is observed" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, ParanormalInvestigator, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", mockName(4), "", ""))
    finalBoard should be (board)

    finalBoard(id(2)).winCondition should be (TannerWinCondition)
    finalBoard(id(2)).seenAs should be (List())

    feedback(id(2)).mkString should include (mockName(4))
    feedback(id(2)).mkString should include regex ("(?i)tanner")

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(5)) should include (mockName(4))
    filtered(0).displayText(SampleUserMapping(5)) should include regex ("(?i)tanner")

  }

  it should "cancel the second action if the first copies a win condition" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, ParanormalInvestigator, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", mockName(4) + " " + mockName(0), "", ""))
    finalBoard should be (board)

    finalBoard(id(2)).winCondition should be (TannerWinCondition)
    finalBoard(id(2)).seenAs should be (List())

    feedback(id(2)).mkString should include (mockName(4))
    feedback(id(2)).mkString should include regex ("(?i)tanner")

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(5)) should include (mockName(4))
    filtered(0).displayText(SampleUserMapping(5)) should include regex ("(?i)tanner")

  }

  it should "run both actions if the second action copies a win condition" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, ParanormalInvestigator, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", mockName(0) + " " + mockName(4), "", ""))
    finalBoard should be (board)

    finalBoard(id(2)).winCondition should be (TannerWinCondition)
    finalBoard(id(2)).seenAs should be (List())

    feedback(id(2)).mkString should include (mockName(0))
    feedback(id(2)).mkString should include regex ("(?i)villager")
    feedback(id(2)).mkString should include (mockName(4))
    feedback(id(2)).mkString should include regex ("(?i)tanner")

    val filtered = filterRecords(history)
    history.toList should have length (3)
    filtered should have length (2)
    filtered(0).displayText(SampleUserMapping(5)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(5)) should include regex ("(?i)villager")
    filtered(1).displayText(SampleUserMapping(5)) should include (mockName(4))
    filtered(1).displayText(SampleUserMapping(5)) should include regex ("(?i)tanner")

  }

  it should "not appear in the werewolf team group message, even if they copy a werewolf" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Werewolf, ParanormalInvestigator, Werewolf, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", mockName(1), "", ""))
    finalBoard should be (board)

    finalBoard(id(2)).winCondition should be (WerewolfWinCondition)
    finalBoard(id(2)).seenAs should be (List(GroupedRoleIdentity.Werewolf))

    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should not include (mockName(2))
    feedback(id(1)).mkString should include (mockName(3))

  }

  it should "not copy a Cursed player" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Werewolf, ParanormalInvestigator, Cursed, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", mockName(3) + " " + mockName(4), "", ""))
    finalBoard should be (board)

    finalBoard(id(2)).winCondition should be (TannerWinCondition)
    finalBoard(id(2)).seenAs should be (List())

  }

  it should "allow a copy-PI to look at the indicated card" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = ParanormalInvestigator,
      playerCards = List(Villager, Copycat, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", ("right", mockName(0)), "", ""))
    finalBoard should be (board)

    finalBoard(id(1)).winCondition should be (TownWinCondition)
    finalBoard(id(1)).seenAs should be (Nil)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include regex ("(?i)villager")
    feedback(id(3)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (2)
    filtered should have length (2)
    filtered(1).displayText(SampleUserMapping(4)) should include (mockName(0))
    filtered(1).displayText(SampleUserMapping(4)) should include regex ("(?i)villager")

  }

  it should "allow a copy-PI to look at the indicated cards" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = ParanormalInvestigator,
      playerCards = List(Villager, Copycat, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", ("right", mockName(0) + " " + mockName(2)), "", ""))
    finalBoard should be (board)

    finalBoard(id(1)).winCondition should be (WerewolfWinCondition)
    finalBoard(id(1)).seenAs should be (List(GroupedRoleIdentity.Werewolf, GroupedRoleIdentity.DreamWolf))

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include regex ("(?i)villager")
    feedback(id(1)).mkString should include regex ("(?i)dream wolf")
    feedback(id(2)) should be (FeedbackMessage.none)
    feedback(id(3)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (4)
    filtered should have length (3)
    filtered(1).displayText(SampleUserMapping(4)) should include (mockName(0))
    filtered(1).displayText(SampleUserMapping(4)) should include regex ("(?i)villager")
    filtered(2).displayText(SampleUserMapping(4)) should include (mockName(2))
    filtered(2).displayText(SampleUserMapping(4)) should include regex ("(?i)dream wolf")

    finalBoard(id(1)).toSnapshot.name should be ("Copycat [Paranormal Investigator [Dream Wolf]]")

  }

  it should "allow a PI to look at and copy a neutral Copycat" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = ParanormalInvestigator,
      playerCards = List(Villager, Copycat, ParanormalInvestigator, Tanner),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", ("middle", ""), mockName(1) + " " + mockName(3), ""))
    finalBoard should be (board)

    finalBoard(id(1)).winCondition should be (WerewolfWinCondition)
    finalBoard(id(1)).seenAs should be (List(GroupedRoleIdentity.Werewolf))

    finalBoard(id(2)).winCondition should be (TownWinCondition)
    finalBoard(id(2)).seenAs should be (List())

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex ("(?i)werewolf")
    feedback(id(2)).mkString should include regex ("(?i)copycat")
    feedback(id(2)).mkString should not include regex ("(?i)tanner") // Copied the copycat, so we don't see the tanner
    feedback(id(2)).mkString should include (mockName(1))
    feedback(id(3)) should be (FeedbackMessage.none)

    finalBoard(id(2)).toSnapshot.name should be ("Paranormal Investigator [Copycat]")

  }

}
