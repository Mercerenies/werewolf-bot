
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import instance.RoleInstance
import util.TextDecorator.*
import util.Grammar
import wincon.{WinCondition, TownWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.{Board, PlayerOrder}
import response.FeedbackMessage
import context.GameContext
import record.ActionPerformedRecord
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Sheep extends Role {

  override class Instance(private val mapping: UserMapping) extends RoleInstance {

    override val role: Sheep.type = Sheep.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[Unit] = {
      import ActionPerformedRecord.*
      for {
        board <- GameContext.getBoard
        order <- GameContext.getPlayerOrder
        seesWerewolves = isWerewolfAdjacent(board, order, userId)
        message <- {
          if (seesWerewolves) {
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("was informed that there is at least one werewolf adjacent to them.")
              })
            } yield {
              FeedbackMessage("There is " + bold("at least one werewolf") + " adjacent to you.")
            }
          } else {
            for {
              _ <- GameContext.record(ActionPerformedRecord(this.toSnapshot, userId) {
                t("was informed that there are no werewolves adjacent to them.")
              })
            } yield {
              FeedbackMessage("There are " + bold("no werewolves") + " adjacent to you.")
            }
          }
        }
        _ <- GameContext.feedback(userId, message)
      } yield {
        ()
      }
    }

    override val winCondition: WinCondition =
      TownWinCondition

  }

  override val name: String = "Sheep"

  override val aliases: List[String] = List()

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.SHEEP

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Sheep.Instance(mapping)

  override val introBlurb: String =
    "You are the " + bold("Sheep") + ". You will be told if there is a werewolf to the immediate left or right of you."

  override def inspiration: Inspiration =
    Inspiration.InspiredBy("Cow", SourceMaterial.Alien)

  def isWerewolfAdjacent(board: Board, order: PlayerOrder, user: Id[User]): Boolean =
    order.adjacentPlayers(user).toList.exists { playerId =>
      board(playerId).seenAs.contains(GroupedRoleIdentity.Werewolf)
    }

}
