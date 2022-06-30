
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import instance.RoleInstance
import util.TextDecorator.*
import wincon.{WinCondition, TannerWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.Board
import response.FeedbackMessage
import context.GameContext
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

object Tanner extends Role {

  override class Instance extends RoleInstance {

    override val role: Tanner.type = Tanner.this

    override val coherenceProof =
      summon[this.type <:< role.Instance]

    override val nightHandler: NightMessageHandler =
      NoInputNightMessageHandler

    override def nightAction(userId: Id[User]): GameContext[Unit] =
      ().point

    override val winCondition: WinCondition =
      TannerWinCondition

  }

  override val name: String = "Tanner"

  override val aliases: List[String] = List("Jester", "Fool")

  override val baseAlignment: Alignment = Alignment.ThirdParty

  override val baseWinCondition: WinCondition = TannerWinCondition

  override val precedence: Int = Precedence.NO_ACTION

  override def createInstance(mapping: UserMapping, initialUserId: Option[Id[User]]): this.Instance =
    Tanner.Instance()

  override val introBlurb: String =
    "You are the " + bold("Tanner") + ". You have no special abilities to activate."

  override def inspiration: Inspiration =
    Inspiration.LiftedFrom(SourceMaterial.Werewolf)

}
