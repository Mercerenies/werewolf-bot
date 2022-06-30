
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import instance.RoleInstance
import util.TextDecorator.*
import wincon.{WinCondition, TownWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.Board
import response.FeedbackMessage
import context.GameContext
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Villager extends Role {

  override class Instance extends RoleInstance {

    override val role: Villager.type = Villager.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[FeedbackMessage] =
      FeedbackMessage.none.point

    override val winCondition: WinCondition =
      TownWinCondition

  }

  override val name: String = "Villager"

  override val aliases: List[String] = List("Townie", "Town", "Vanilla")

  override val baseAlignment: Alignment = Alignment.Town

  override val baseWinCondition: WinCondition = TownWinCondition

  override val precedence: Int = Precedence.NO_ACTION

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Villager.Instance()

  override val introBlurb: String =
    "You are a " + bold("Villager") + ". You have no special abilities to activate."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Werewolf)

}
