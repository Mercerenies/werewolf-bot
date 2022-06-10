
package com.mercerenies.werewolf
package game
package role

import id.{Id, UserMapping}
import util.TextDecorator.*
import wincon.{WinCondition, TownWinCondition}
import night.{NightMessageHandler, NoInputNightMessageHandler}
import board.Board
import response.FeedbackMessage

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

    override def nightAction(mapping: UserMapping, userId: Id[User]): State[Board, FeedbackMessage] =
      FeedbackMessage.none.point


  }

  override val name: String = "Villager"

  override val aliases: List[String] = List("Townie", "Town", "Vanilla")

  override val baseAlignment: Alignment = Alignment.Town

  override val precedence: Int = Precedence.NO_ACTION

  override def createInstance(): this.Instance =
    Villager.Instance()

  override val introBlurb: String =
    "You are a " + bold("Villager") + ". You have no special abilities to activate."

  override val winCondition: WinCondition =
    TownWinCondition

}
