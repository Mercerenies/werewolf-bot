
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity
import id.{Id, UserMapping}
import instance.RoleInstance
import util.TextDecorator.*
import wincon.WinCondition
import source.{Inspiration, SourceMaterial}

import org.javacord.api.entity.user.User

trait Role extends NamedEntity {

  type Instance <: RoleInstance

  def precedence: Int

  // A role's base alignment, without modifications from the night
  // actions. For example, a doppelganger always shows its
  // baseAlignment as ThirdParty, ignoring whatever role it's copied.
  // A Paranormal Investigator shows its baseAlignment as Town, since
  // it won't take into consideration the viewed cards.
  //
  // The baseAlignment is used for cards that need a naive
  // interpretation if "is this role town". For instance, a Revealer
  // will only reveal roles whose baseAlignment is Town. Likewise, a
  // Paranormal Investigator will always stop and copy the properties
  // of a role whose baseAlignment is non-town.
  def baseAlignment: Alignment

  // A role's base win condition, without modifications from the night
  // actions. This is the win condition that shall be used if the role
  // is put into play from the center by e.g. a Witch or a Drunk.
  def baseWinCondition: WinCondition

  def baseSeenAs: List[GroupedRoleIdentity] =
    Nil

  def createInstance(mapping: UserMapping, initialUser: Option[Id[User]]): this.Instance

  def introBlurb: String

  def inspiration: Inspiration

}

object Role {

  import FluffyRipper.{Fluffy, Ripper}

  val all: List[Role] = List(
    Bodyguard,
    DreamWolf,
    Drunk,
    Exposer,
    Fluffy,
    Hunter,
    Insomniac,
    Mason,
    Minion,
    ParanormalInvestigator,
    Revealer,
    Ripper,
    Robber,
    Seer,
    Sheep,
    Tanner,
    Troublemaker,
    Villager,
    Werewolf,
    Witch,
  )

  def formattedList(roleList: Iterable[Role]): String =
    longCode(
      roleList.map(_.name).mkString("\n"),
    )

}
