
package com.mercerenies.werewolf
package game
package role

import org.javacord.api.entity.user.User

import org.scalatestplus.mockito.MockitoSugar

import id.{UserMapping, Id}
import state.NightPhaseState
import response.FeedbackMessage
import board.{Board, TablePosition, Position}

class DreamWolfSpec extends GameplayUnitSpec {

  "The Dream Wolf role" should "be included in the global role list" in {
    Role.all should contain (DreamWolf)
  }

  it should "not change the state of the game" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Villager,
      playerCards = List(Werewolf, Villager, DreamWolf),
    )
    val (finalBoard, _, _, reveals) = playGame(board, List("", "", ""))
    finalBoard should be (board)
    reveals shouldBe empty
  }

  it should "count as a werewolf but not be informed of the other werewolves" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, DreamWolf),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)).mkString should include (mockName(0))
    feedback(id(0)).mkString should include (mockName(1))
    feedback(id(0)).mkString should include (mockName(2))
    feedback(id(1)).mkString should include (mockName(0))
    feedback(id(1)).mkString should include (mockName(1))
    feedback(id(1)).mkString should include (mockName(2))
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (4) // Dream wolf doesn't get a message.

    // First message is to player 0 and indicates the werewolf team
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(2))

    // Second message is to player 0 and indicates the dream wolf
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wolves")
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(2))

    // Third message is to player 1 and indicates the werewolf team
    filtered(2).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(2).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(2).displayText(SampleUserMapping(3)) should include (mockName(2))

    // Fourthmessage is to player 0 and indicates the dream wolf
    filtered(3).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wolves")
    filtered(3).displayText(SampleUserMapping(3)) should include (mockName(2))

  }

  it should "mention a dream wolf message if there are dream wolf cards in the center" in {
    val board = createBoard(
      left = Werewolf,
      middle = DreamWolf,
      right = Tanner,
      playerCards = List(Werewolf, Werewolf, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)).mkString should include regex ("(?i)dream wol(f|ves)")
    feedback(id(1)).mkString should include regex ("(?i)dream wol(f|ves)")
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (4)

    // Second message is to player 0 and indicates the (lack of) dream wolves
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wol(f|ves)")
    filtered(1).displayText(SampleUserMapping(3)) should not include (mockName(1))
    filtered(1).displayText(SampleUserMapping(3)) should not include (mockName(2))

    // Fourth message is to player 0 and indicates the (lack of) dream wolves
    filtered(3).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wol(f|ves)")
    filtered(3).displayText(SampleUserMapping(3)) should not include (mockName(0))
    filtered(3).displayText(SampleUserMapping(3)) should not include (mockName(2))

  }

  it should "mention a dream wolf team to a non- dream wolf" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Werewolf, DreamWolf, DreamWolf),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    val feedback0 = feedback(id(0)).toList
    feedback0 should have length (2)

    feedback0(0) should include (mockName(0))
    feedback0(0) should include (mockName(1))
    feedback0(0) should include (mockName(2))
    feedback0(1) should include regex ("(?i)dream wolves")
    feedback0(1) should not include (mockName(0))
    feedback0(1) should include (mockName(1))
    feedback0(1) should include (mockName(2))

    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (2) // Dream wolves don't get a message.

    // First message is to player 0 and indicates the werewolf team
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(0).displayText(SampleUserMapping(3)) should include (mockName(2))

    // Second message is to player 0 and indicates the dream wolf
    filtered(1).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wolves")
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(2))

  }

  it should "provide no feedback to a solo dream wolf" in {
    val board = createBoard(
      left = Villager,
      middle = Villager,
      right = Tanner,
      playerCards = List(Villager, DreamWolf, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (0)

  }

  it should "provide no feedback to a solo copycat dream wolf" in {
    val board = createBoard(
      left = Villager,
      middle = DreamWolf,
      right = Tanner,
      playerCards = List(Villager, Copycat, Villager),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", ("middle", ""), ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none) // Only interactive feedback
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wolf")

  }

  it should "provide no feedback to a team of a dream wolf and a copycat dream wolf" in {
    val board = createBoard(
      left = Villager,
      middle = DreamWolf,
      right = Tanner,
      playerCards = List(Villager, Copycat, DreamWolf),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", ("middle", ""), ""))
    finalBoard should be (board)

    feedback(id(0)) should be (FeedbackMessage.none)
    feedback(id(1)) should be (FeedbackMessage.none) // Only interactive feedback
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (1)
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wolf")

  }

  it should "mention a dream wolf team (including copycats who copied dream wolves) to a non- dream wolf" in {
    val board = createBoard(
      left = Werewolf,
      middle = DreamWolf,
      right = Tanner,
      playerCards = List(Werewolf, DreamWolf, Copycat),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List("", "", ("middle", "")))
    finalBoard should be (board)

    val feedback0 = feedback(id(0)).toList
    feedback0 should have length (2)

    feedback0(0) should include (mockName(0))
    feedback0(0) should include (mockName(1))
    feedback0(0) should include (mockName(2))
    feedback0(1) should include regex ("(?i)dream wolves")
    feedback0(1) should not include (mockName(0))
    feedback0(1) should include (mockName(1))
    feedback0(1) should include (mockName(2))

    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (3) // Dream wolves don't get a message.

    // Copycat message
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wolf")
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)middle")

    // Second message is to player 0 and indicates the werewolf team
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(2))

    // Third message is to player 0 and indicates the dream wolf
    filtered(2).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wolves")
    filtered(2).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(2).displayText(SampleUserMapping(3)) should include (mockName(2))

  }

  it should "mention a dream wolf team to a copycat who copied an ordinary werewolf" in {
    val board = createBoard(
      left = Werewolf,
      middle = Werewolf,
      right = Tanner,
      playerCards = List(Copycat, DreamWolf, DreamWolf),
    )
    val (finalBoard, history, feedback, _) = playGame(board, List(("left", ""), "", ""))
    finalBoard should be (board)

    val feedback0 = feedback(id(0)).toList
    feedback0 should have length (2)

    feedback0(0) should include (mockName(0))
    feedback0(0) should include (mockName(1))
    feedback0(0) should include (mockName(2))
    feedback0(1) should include regex ("(?i)dream wolves")
    feedback0(1) should not include (mockName(0))
    feedback0(1) should include (mockName(1))
    feedback0(1) should include (mockName(2))

    feedback(id(1)) should be (FeedbackMessage.none)
    feedback(id(2)) should be (FeedbackMessage.none)

    val filtered = filterRecords(history)
    filtered should have length (3) // Dream wolves don't get a message.

    // First message is the copycat
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)werewolf")
    filtered(0).displayText(SampleUserMapping(3)) should include regex ("(?i)left")

    // Second message is to player 0 and indicates the werewolf team
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(0))
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(1).displayText(SampleUserMapping(3)) should include (mockName(2))

    // Third message is to player 0 and indicates the dream wolf
    filtered(2).displayText(SampleUserMapping(3)) should include regex ("(?i)dream wolves")
    filtered(2).displayText(SampleUserMapping(3)) should include (mockName(1))
    filtered(2).displayText(SampleUserMapping(3)) should include (mockName(2))

  }

}
