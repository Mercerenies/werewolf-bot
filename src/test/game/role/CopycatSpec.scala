
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import wincon.*
import state.NightPhaseState
import response.{FeedbackMessage, ReplyResponse}
import board.{Board, TablePosition, Position}

class CopycatSpec extends GameplayUnitSpec {

  "The Copycat role" should "be included in the global role list" in {
    Role.all should contain (Copycat)
  }

  it should "look at the indicated card and become a copy of it" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Copycat, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", ("right", ""), "", ""))
    finalBoard should be (board)

    finalBoard(id(1)).asInstanceOf[Copycat.Instance].copiedRole.map(_.role) should be (Some(Villager))
    finalBoard(id(1)).winCondition should be (TownWinCondition)
    finalBoard(id(1)).seenAs should be (Nil)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)
    feedback(id(3)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (1)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(4)) should include regex ("(?i)right")
    filtered(0).displayText(SampleUserMapping(4)) should include regex ("(?i)villager")

  }

  it should "receive feedback about the indicated card" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Copycat, DreamWolf, Tanner),
    )

    val response = board(id(1)).asInstanceOf[Copycat.Instance].duskHandler.onDirectMessage(board, "right")
    response shouldBe a [ReplyResponse]
    response.asInstanceOf[ReplyResponse].replyText should include regex ("(?i)right")
    response.asInstanceOf[ReplyResponse].replyText should include regex ("(?i)villager")

  }

/*
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
 */
}
