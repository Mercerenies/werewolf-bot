
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

  it should "look at the left card if no feedback is received" in {
    val board = createBoard(
      left = Werewolf,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Copycat, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", ("", ""), "", ""))
    finalBoard should be (board)

    finalBoard(id(1)).asInstanceOf[Copycat.Instance].copiedRole.map(_.role) should be (Some(Werewolf))
    finalBoard(id(1)).winCondition should be (WerewolfWinCondition)
    finalBoard(id(1)).seenAs should be (List(GroupedRoleIdentity.Werewolf))

    // If it's a passive copy, then the new role message is part of
    // FeedbackMessage since it's not interactive.
    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)).mkString should include regex ("(?i)left")
    feedback(id(1)).mkString should include regex ("(?i)werewolf")
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(2)) should be (FeedbackMessage.none)
    feedback(id(3)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    history.toList should have length (3)
    filtered should have length (3)

    // Copycat copies werewolf
    filtered(0).displayText(SampleUserMapping(4)) should include regex ("(?i)left")
    filtered(0).displayText(SampleUserMapping(4)) should include regex ("(?i)werewolf")

    // Copy-werewolf is told about the other werewolf
    filtered(1).displayText(SampleUserMapping(4)) should include (mockName(1))
    filtered(1).displayText(SampleUserMapping(4)) should include (mockName(2))
    filtered(1).displayText(SampleUserMapping(4)) should include regex ("(?i)werewolf")

    // Copy-werewolf is told about the dream wolf
    filtered(2).displayText(SampleUserMapping(4)) should include (mockName(2))
    filtered(2).displayText(SampleUserMapping(4)) should include regex ("(?i)dream wol(f|ves)")

  }

  it should "not create an infinite loop if the left card is Copycat and no response is received" in {
    val board = createBoard(
      left = Copycat,
      middle = Villager,
      right = Villager,
      playerCards = List(Villager, Copycat, DreamWolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", ("", ""), "", ""))
    finalBoard should be (board)

    // Note: I consider games with two copycat cards invalid, for
    // basically this reason. If a player forces this game to occur,
    // all I care is that the game doesn't crash.

  }

  it should "receive feedback about the indicated card" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Copycat, DreamWolf, Tanner),
    )

    // Normally, the feedback is interactive. So it won't show up in
    // the FeedbackMessage and is instead sent directly.
    val response = board(id(1)).asInstanceOf[Copycat.Instance].duskHandler.onDirectMessage(board, "right")
    response shouldBe a [ReplyResponse]
    response.asInstanceOf[ReplyResponse].replyText should include regex ("(?i)right")
    response.asInstanceOf[ReplyResponse].replyText should include regex ("(?i)villager")

  }

  it should "transform into a werewolf if one is observed" in {
    val board = createBoard(
      left = Villager,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, Copycat, Werewolf, Tanner),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ("middle", ""), "", ""))
    finalBoard should be (board)

    finalBoard(id(2)).winCondition should be (WerewolfWinCondition)
    finalBoard(id(2)).seenAs should be (List(GroupedRoleIdentity.Werewolf))

    // Werewolf team is informed of the werewolf team, including copycat
    feedback(id(2)).mkString should include (mockName(2))
    feedback(id(2)).mkString should include (mockName(3))
    feedback(id(2)).mkString should include regex ("(?i)werewolf")
    feedback(id(3)).mkString should include (mockName(2))
    feedback(id(3)).mkString should include (mockName(3))
    feedback(id(3)).mkString should include regex ("(?i)werewolf")

    val filtered = filterRecords(history)
    history.toList should have length (3)
    filtered should have length (3)

    // Copycat's copy message
    filtered(0).displayText(SampleUserMapping(5)) should include (mockName(2))
    filtered(0).displayText(SampleUserMapping(5)) should include regex ("(?i)werewolf")

    // One werewolf is informed of the other
    filtered(1).displayText(SampleUserMapping(5)) should include (mockName(2))
    filtered(1).displayText(SampleUserMapping(5)) should include (mockName(3))
    filtered(1).displayText(SampleUserMapping(5)) should include regex ("(?i)werewolf")

    // The other werewolf is also informed
    filtered(2).displayText(SampleUserMapping(5)) should include (mockName(2))
    filtered(2).displayText(SampleUserMapping(5)) should include (mockName(3))
    filtered(2).displayText(SampleUserMapping(5)) should include regex ("(?i)werewolf")

  }

  it should "count as a town card if put into play from the center by a witch" in {
    val board = createBoard(
      left = Copycat,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Villager, Villager, DreamWolf, Witch),
    )
    val (finalBoard, _, feedback, _) = playGame(board, List("", "", "", "left " + mockName(0)))
    finalBoard(TablePosition.Left).role should be (Villager)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Copycat)
    finalBoard(id(1)).role should be (Villager)
    finalBoard(id(2)).role should be (DreamWolf)
    finalBoard(id(3)).role should be (Witch)

    finalBoard(id(0)).asInstanceOf[Copycat.Instance].copiedRole.map(_.role) should be (None)
    finalBoard(id(0)).winCondition should be (TownWinCondition)
    finalBoard(id(0)).seenAs should be (Nil)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)
    feedback(id(3)).mkString should include regex ("(?i)left")
    feedback(id(3)).mkString should include regex ("(?i)copycat")
    feedback(id(3)).mkString should include (mockName(0))

  }

  it should "count as the copied role if swapped through the center and back out by a witch and a drunk" in {
    val board = createBoard(
      left = Tanner,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Copycat, Drunk, DreamWolf, Witch),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List(("left", ""), "left", "", "left " + mockName(0)))
    finalBoard(TablePosition.Left).role should be (Drunk)
    finalBoard(TablePosition.Middle).role should be (Werewolf)
    finalBoard(TablePosition.Right).role should be (Villager)
    finalBoard(id(0)).role should be (Tanner)
    finalBoard(id(1)).role should be (Copycat)
    finalBoard(id(2)).role should be (DreamWolf)
    finalBoard(id(3)).role should be (Witch)

    finalBoard(id(1)).asInstanceOf[Copycat.Instance].copiedRole.map(_.role) should be (Some(Tanner))
    finalBoard(id(1)).winCondition should be (TannerWinCondition)
    finalBoard(id(1)).seenAs should be (Nil)

    feedback(id(0)) should be (FeedbackMessage.none) // Copycat only gets interactive feedback
    feedback(id(1)).mkString should include regex ("(?i)left")
    feedback(id(2)) should be (FeedbackMessage.none)
    feedback(id(3)).mkString should include regex ("(?i)left")
    feedback(id(3)).mkString should include regex ("(?i)tanner")
    feedback(id(3)).mkString should include (mockName(0))

    val filtered = filterRecords(history)
    history.toList should have length (5)
    filtered should have length (3)

    // Copycat copies tanner
    filtered(0).displayText(SampleUserMapping(4)) should include regex ("(?i)copycat")
    filtered(0).displayText(SampleUserMapping(4)) should include regex ("(?i)tanner")

    // Witch swaps the copycat and tanner cards
    filtered(1).displayText(SampleUserMapping(4)) should include (mockName(0))
    filtered(1).displayText(SampleUserMapping(4)) should include regex ("(?i)tanner")

    // Drunk swaps with the copycat card (which is now treated as a tanner card)
    filtered(2).displayText(SampleUserMapping(4)) should include regex ("(?i)left")
    filtered(2).displayText(SampleUserMapping(4)) should not include regex ("(?i)tanner|copycat|witch")

  }

}
