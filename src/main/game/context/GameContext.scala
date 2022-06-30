
package com.mercerenies.werewolf
package game
package context

import id.Id
import board.{Board, Position, PlayerOrder}
import record.{RecordedGameHistory, GameRecord, SnapshotRecord}
import response.FeedbackMessage

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

  def run(board: Board, playerOrder: PlayerOrder, history: RecordedGameHistory): ContextResult[A] = {
    val wba = impl(ContextState(board, playerOrder))
    val (newHistory, (state, a)) = wba.run
    // Note: We ignore state.userIds, since none of the public
    // functions in this file modify that, so it's effectively a
    // reader.
    ContextResult(
      board = state.board,
      history = history ++ newHistory,
      revealedCards = state.revealedCards,
      playerFeedback = state.playerFeedback,
      result = a,
    )
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

  val getPlayerOrder: GameContext[PlayerOrder] =
    GameContext(StateT.gets { _.playerOrder })

  def perform[A](arg: => A): GameContext[A] =
    // Used to capture mutable state inside this monad: perform an
    // action (that presumably mutates some data) and return the
    // result.
    GameContext(StateT { s =>
      val result = arg
      (s, result).point
    })

  def setBoard(newBoard: Board): GameContext[Unit] =
    GameContext(StateT.modify { _.copy(board = newBoard) })

  def modifyBoard(fn: (Board) => Board): GameContext[Unit] =
    getBoard >>= { board => setBoard(fn(board)) }

  def record(rec: GameRecord*): GameContext[Unit] =
    GameContext(RecordedGameHistory.from(rec).tell.liftM)

  def revealCard(pos: Position): GameContext[Unit] =
    GameContext(StateT.modify { _.withRevealed(pos) })

  def feedback(user: Id[User], message: FeedbackMessage): GameContext[Unit] =
    GameContext(StateT.modify { _.feedback(user, message) })

  def feedback(user: Id[User], first: String, rest: String*): GameContext[Unit] =
    feedback(user, FeedbackMessage.messages(first :: rest.toList))

  val recordCurrentBoard: GameContext[Unit] =
    for {
      board <- getBoard
      order <- getPlayerOrder
      snapshot = board.toSnapshot(order)
      _ <- record(SnapshotRecord(snapshot))
    } yield {
      ()
    }

  given GameContextIsMonad : Monad[GameContext] with

    def point[A](a: => A) =
      GameContext(a.point)

    def bind[A, B](fa: GameContext[A])(f: A => GameContext[B]) =
      GameContext(fa.impl >>= { a => f(a).impl })

}
