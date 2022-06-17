
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
  private val impl: StateT[Board, Writer[RecordedGameHistory, _], A],
) {

  def run(board: Board, history: RecordedGameHistory): (Board, RecordedGameHistory, A) = {
    val wba = impl(board)
    val (newHistory, (finalBoard, a)) = wba.run
    (finalBoard, history ++ newHistory, a)
  }

  def censorRecords(fn: (RecordedGameHistory) => RecordedGameHistory): GameContext[A] = {
    // Apply .colocal to the inner Writer. Does not affect the StateT
    // part.
    type Impl[F[_], B] = StateT[Board, F, B]
    val transform = Hoist[Impl].hoist(GameContext.CensorTransform(fn))
    GameContext(transform(impl))
  }

}

object GameContext {

  // WriterT.colocal, as a natural transformation.
  private class CensorTransform[W1, W2](
    val fn: (W1) => W2,
  ) extends NaturalTransformation[Writer[W1, _], Writer[W2, _]] {
    def apply[A](m: Writer[W1, A]): Writer[W2, A] =
      m.colocal(fn)
  }

  val getBoard: GameContext[Board] =
    GameContext(StateT.get)

  def setBoard(newBoard: Board): GameContext[Unit] =
    GameContext(StateT.put(newBoard))

  def modifyBoard(fn: (Board) => Board): GameContext[Unit] =
    getBoard >>= { board => setBoard(fn(board)) }

  def record(rec: GameRecord*): GameContext[Unit] =
    GameContext(RecordedGameHistory.from(rec).tell.liftM)

  given GameContextIsMonad : Monad[GameContext] with

    def point[A](a: => A) =
      GameContext(a.point)

    def bind[A, B](fa: GameContext[A])(f: A => GameContext[B]) =
      GameContext(fa.impl >>= { a => f(a).impl })

}
