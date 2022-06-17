
package com.mercerenies.werewolf
package game
package context

import board.BoardTestUtil.*
import board.{Board, Position, TablePosition}
import id.Id
import role.*
import record.RecordedGameHistory

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import org.javacord.api.entity.user.User

class GameContextSpec extends UnitSpec {

  // Generally, for this class, we don't care about the actual roles;
  // We just care that what we get out is what we put in.
  def sampleBoard(): Board =
    createBoard(Villager, Werewolf, Villager, List(Tanner, Tanner, Tanner))

  "The GameContext monad" should "provide access to the underlying board when asked" in {
    val board = sampleBoard()

    val (b1, history, b2) = GameContext.getBoard.run(board, RecordedGameHistory.empty)
    history.toVector shouldBe empty
    b1 should be (board)
    b2 should be (board)

  }

  it should "allow modification to the underlying board with setBoard" in {
    val board1 = sampleBoard()
    val board2 = board1.swap(Position.left, Position.middle)
    val m = for {
      originalBoard <- GameContext.getBoard
      _ <- GameContext.setBoard(board2)
      modifiedBoard <- GameContext.getBoard
    } yield {
      (originalBoard, modifiedBoard)
    }
    val (finalState, history, (originalBoard, modifiedBoard)) = m.run(board1, RecordedGameHistory.empty)
    history.toVector shouldBe empty
    finalState should be (board2)
    modifiedBoard should be (board2)
    originalBoard should be (board1)
  }

  it should "allow modification to the underlying board with modifyBoard" in {
    val board1 = sampleBoard()
    val board2 = board1.swap(Position.left, Position.middle)
    val m = for {
      originalBoard <- GameContext.getBoard
      _ <- GameContext.modifyBoard(_.swap(Position.left, Position.middle))
      modifiedBoard <- GameContext.getBoard
    } yield {
      (originalBoard, modifiedBoard)
    }
    val (finalState, history, (originalBoard, modifiedBoard)) = m.run(board1, RecordedGameHistory.empty)
    history.toVector shouldBe empty
    finalState should be (board2)
    modifiedBoard should be (board2)
    originalBoard should be (board1)
  }

  it should "record GameRecord instances that occur over the course of the game" in {
    val board = sampleBoard()
    
  }

}
