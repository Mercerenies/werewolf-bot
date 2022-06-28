
package com.mercerenies.werewolf
package game
package votes
package context

import record.{RecordedGameHistory, GameRecord, SnapshotRecord}
import id.Id

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import scala.math.Ordering.Implicits.*

// Note: I'm being very careful in what functions I introduce here.
// This monad is going to be run in a loop, and I want to guarantee
// that that loop will always terminate. As such, the following
// guarantees are made.
//
// (1) All functions which modify the DeathRoster do so in an
// idempotent way.
//
// (2) Functions only modify the DeathRoster by increasing the
// DeathStatus of characters. There are no functions here to add new
// players to the roster, nor are there functions to decrease
// positions on the roster.
//
// As a convenience (for more convenient use with
// RecordedGameHistory), these functions will generally return a
// Boolean indicating whether something was changed. If this Boolean
// is false, then no values were changed.

final class VotesContext[A] private(
  private val impl: StateT[VotesContextState, Writer[RecordedGameHistory, _], A],
) {

  def run(votals: Votals[Id[User]], roster: DeathRoster[Id[User]], history: RecordedGameHistory): VotesContextResult[A] = {
    val wba = impl(VotesContextState(votals, roster))
    val (newHistory, (state, a)) = wba.run
    // Note: We ignore state.votals, since none of the public
    // functions in this file modify that, so it's effectively a
    // reader.
    VotesContextResult(
      deaths = state.deaths,
      history = history ++ newHistory,
      result = a,
    )
  }

}

object VotesContext {

  val getDeathRoster: VotesContext[DeathRoster[Id[User]]] =
    VotesContext(StateT.gets { _.deaths })

  val getVotals: VotesContext[Votals[Id[User]]] =
    VotesContext(StateT.gets { _.votals })

  def updatePlayer(user: Id[User], deathStatus: DeathStatus): VotesContext[Boolean] =
    for {
      roster <- getDeathRoster
      currentValue = roster.get(user)
      result <- currentValue match {
        case None => {
          // Not found, don't update anything
          false.point
        }
        case Some(x) if x >= deathStatus => {
          // Found, but already higher
          false.point
        }
        case Some(_) =>
          // Needs to be updated
          updatePlayerUnchecked(user, deathStatus) >| true
      }
    } yield {
      result
    }

  private def updatePlayerUnchecked(user: Id[User], deathStatus: DeathStatus): VotesContext[Unit] =
    VotesContext(StateT.modify { state => state.copy(deaths = state.deaths.updated(user, deathStatus)) })

  def killPlayer(user: Id[User]): VotesContext[Boolean] =
    updatePlayer(user, DeathStatus.Dead)

  def protectPlayer(user: Id[User]): VotesContext[Boolean] =
    updatePlayer(user, DeathStatus.Protected)

  def record(rec: GameRecord*): VotesContext[Unit] =
    VotesContext(RecordedGameHistory.from(rec).tell.liftM)

  given VotesContextIsMonad : Monad[VotesContext] with

    def point[A](a: => A) =
      VotesContext(a.point)

    def bind[A, B](fa: VotesContext[A])(f: A => VotesContext[B]) =
      VotesContext(fa.impl >>= { a => f(a).impl })

}
