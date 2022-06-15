
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

}
