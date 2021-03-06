
package com.mercerenies.werewolf
package game

import choice.Choice
import choice.syntax.*
import parser.assignment.NamedUser
import board.{Board, TablePosition}
import id.{UserMapping, Id}

import org.javacord.api.entity.user.User

// Helpers for constructing Choice instances based on game data.
final class ChoiceFactory(namedUsers: List[NamedUser]) {

  // TODO This will use the board to determine alpha wolf issues...
  def tablePosition: Choice[TablePosition] =
    oneOf(TablePosition.all)

  def twoTablePositions: Choice[(TablePosition, TablePosition)] =
    twoOf(TablePosition.all)

  def player: Choice[NamedUser] =
    oneOf(namedUsers)

  def playerNotSelf(self: Id[User]): Choice[NamedUser] =
    oneOf(namedUsers.filter { _.id != self })

  def playerNotSelfOption(self: Option[Id[User]]): Choice[NamedUser] =
    self match {
      case None => player
      case Some(userId) => playerNotSelf(userId)
    }

  def twoPlayers: Choice[(NamedUser, NamedUser)] =
    twoOf(namedUsers)

  def twoPlayersNotSelf(self: Id[User]): Choice[(NamedUser, NamedUser)] =
    twoOf(namedUsers.filter { _.id != self })

  def twoPlayersNotSelfOption(self: Option[Id[User]]): Choice[(NamedUser, NamedUser)] =
    self match {
      case None => twoPlayers
      case Some(userId) => twoPlayersNotSelf(userId)
    }

  def oneOrTwoPlayers: Choice[(NamedUser, Option[NamedUser])] =
    oneOrTwoOf(namedUsers)

  def oneOrTwoPlayersNotSelf(self: Id[User]): Choice[(NamedUser, Option[NamedUser])] =
    oneOrTwoOf(namedUsers.filter { _.id != self })

  def oneOrTwoPlayersNotSelfOption(self: Option[Id[User]]): Choice[(NamedUser, Option[NamedUser])] =
    self match {
      case None => oneOrTwoPlayers
      case Some(userId) => oneOrTwoPlayersNotSelf(userId)
    }

  def direction: Choice[Direction] =
    oneOf(Direction.all)

}
