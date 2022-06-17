
package com.mercerenies.werewolf
package game
package context

import id.Id
import board.Board
import record.{RecordedGameHistory, GameRecord, SnapshotRecord}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

// A state-like monad which stores all of the relevant information
// necessary to perform the night phase of the game. Note that
// *currently* this is implemented on top of scalaz.State, but I
// provide no direct access to that implementation and it may change
// in the future.
final class GameContext[A] private(
  private val impl: StateT[ContextState, Writer[RecordedGameHistory, _], A],
) {

  def run(board: Board, ids: List[Id[User]], history: RecordedGameHistory): (Board, RecordedGameHistory, A) = {
    val wba = impl(ContextState(board, ids))
    val (newHistory, (state, a)) = wba.run
    // Note: We ignore state.userIds, since none of the public
    // functions in this file modify that, so it's effectively a
    // reader.
    (state.board, history ++ newHistory, a)
  }

  def censorRecords(fn: (RecordedGameHistory) => RecordedGameHistory): GameContext[A] = {
    // Apply .colocal to the inner Writer. Does not affect the StateT
    // part.
    type Impl[F[_], B] = StateT[ContextState, F, B]
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
    GameContext(StateT.gets { _.board })

  val getUserIds: GameContext[List[Id[User]]] =
    GameContext(StateT.gets { _.userIds })

  def setBoard(newBoard: Board): GameContext[Unit] =
    GameContext(StateT.modify { _.copy(board = newBoard) })

  def modifyBoard(fn: (Board) => Board): GameContext[Unit] =
    getBoard >>= { board => setBoard(fn(board)) }

  def record(rec: GameRecord*): GameContext[Unit] =
    GameContext(RecordedGameHistory.from(rec).tell.liftM)

  /*
  def recordCurrentBoard(ids: List[Id[User]]): GameContext[Unit] =
    for {
      board <- getBoard
      snapshot = board.toSnapshot(ids)
      _ <- record(SnapshotRecord(snapshot))
    } yield {
      ()
    }
   */

  given GameContextIsMonad : Monad[GameContext] with

    def point[A](a: => A) =
      GameContext(a.point)

    def bind[A, B](fa: GameContext[A])(f: A => GameContext[B]) =
      GameContext(fa.impl >>= { a => f(a).impl })

}
