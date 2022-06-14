
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import util.TextDecorator.*
import wincon.{WinCondition, TannerWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.Board
import response.FeedbackMessage

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

    override def nightAction(mapping: UserMapping, userId: Id[User]): State[Board, FeedbackMessage] =
      FeedbackMessage.none.point

    override val winCondition: WinCondition =
      TannerWinCondition

  }

  override val name: String = "Tanner"

  override val aliases: List[String] = List("Jester", "Fool")

  override val baseAlignment: Alignment = Alignment.ThirdParty

  override val precedence: Int = Precedence.NO_ACTION

  override def createInstance(): this.Instance =
    Tanner.Instance()

  override val introBlurb: String =
    "You are a " + bold("Tanner") + ". You have no special abilities to activate."

}