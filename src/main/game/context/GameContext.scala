
package com.mercerenies.werewolf
package game
package context

import board.Board
import record.{RecordedGameHistory, GameRecord}

import scalaz.*
import Scalaz.*

// A state-like monad which stores all of the relevant information
// necessary to perform the night phase of the game. Note that
// *currently* this is implemented on top of scalaz.State, but I
// provide no direct access to that implementation and it may change
// in the future.
final class GameContext[A] private(
  private val impl: State[ContextState, A],
) {

  def run(board: Board, history: RecordedGameHistory): (Board, RecordedGameHistory, A) = {
    val (state, a) = impl(ContextState(history, board))
    (state.board, state.history, a)
  }

}

object GameContext {

  val getBoard: GameContext[Board] =
    GameContext(State.gets { _.board })

  def setBoard(newBoard: Board): GameContext[Unit] =
    GameContext(State.modify { _.copy(board = newBoard) })

  def modifyBoard(fn: (Board) => Board): GameContext[Unit] =
    getBoard >>= { board => setBoard(fn(board)) }

  def record(rec: GameRecord*): GameContext[Unit] =
    GameContext(State.modify { state =>
      state.copy(history = state.history ++ rec)
    })

  given GameContextIsMonad : Monad[GameContext] with

    def point[A](a: => A) =
      GameContext(a.point)

    def bind[A, B](fa: GameContext[A])(f: A => GameContext[B]) =
      GameContext(fa.impl >>= { a => f(a).impl })

}
