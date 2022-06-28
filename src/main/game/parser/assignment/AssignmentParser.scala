
package com.mercerenies.werewolf
package game
package parser
package assignment

import id.Id
import id.Ids.*
import name.{NamedEntity, NamedEntityMatcher}
import board.{Board, Position, TablePosition, PlayerOrder}
import role.Role

import org.javacord.api.DiscordApi
import org.javacord.api.entity.user.User
import org.javacord.api.entity.server.Server

import scala.concurrent.{Future, ExecutionContext}

class AssignmentParser(
  private val roles: List[MaybeRole],
  private val positions: List[NamedPosition],
) {

  private val roleMatcher: NamedEntityMatcher[MaybeRole] =
    NamedEntityMatcher(roles)

  private val positionMatcher: NamedEntityMatcher[NamedPosition] =
    NamedEntityMatcher(positions)

  def parse(text: String): Option[Assignment] = {
    val roleMatches = roleMatcher.findAll(text).toList
    val positionMatches = positionMatcher.findAll(text).toList
    if ((roleMatches.size == 1) && (positionMatches.size == 1)) {
      Some(Assignment(positionMatches(0), roleMatches(0)))
    } else {
      None
    }
  }

  // Look for blocks of bold and parse each one separately for an
  // Assignment command. Only the successful parses are kept, so this
  // method returns Nil if there are no successful parses (or if there
  // are no bold blocks to begin with)
  def parseFullMessage(text: String): List[Assignment] =
    Parsing.findBoldText(text).flatMap(parse)

}

object AssignmentParser {

  def fromBoard(api: DiscordApi, server: Server, board: Board, order: PlayerOrder)(using ExecutionContext): Future[AssignmentParser] = {
    val roles = MaybeRole.empty :: board.roles.map(MaybeRole.fromRole)
    for {
      positions <- NamedPosition.all(api, server, board, order)
    } yield {
      AssignmentParser(roles, positions)
    }
  }
}
