
package com.mercerenies.werewolf
package game
package parser
package assignment

import id.Id
import id.Ids.*
import name.NamedEntity
import board.{Board, Position, TablePosition, PlayerOrder}

import org.javacord.api.DiscordApi
import org.javacord.api.entity.user.User
import org.javacord.api.entity.server.Server

import scalaz.{Id => _, *}
import Scalaz.{Id => _, *}

import scala.jdk.OptionConverters.*
import scala.concurrent.{Future, ExecutionContext}

// A game.board.Position but associated with a given name.
sealed abstract class NamedPosition extends NamedEntity {

  def toPosition: Position

}

object NamedPosition {

  case class Table(val side: TablePosition) extends NamedPosition {
    export side.{name, aliases}

    def toPosition = Position.Table(side)

  }

  case class Player(val player: NamedUser) extends NamedPosition {
    export player.{name, aliases}

    def toPosition = Position.Player(player.id)

  }

  // Note: We don't use the board right now, but we will once roles
  // like Alpha Wolf can introduce new "center of table" cards.
  def all(api: DiscordApi, server: Server, board: Board, order: PlayerOrder)(using ExecutionContext): Future[List[NamedPosition]] = {
    val tablePositions = TablePosition.all.map { Table(_) }
    for {
      users <- order.toList.traverse { api.getUser(_) }
    } yield {
      val namedUsers = users.map { user => Player(NamedUser.fromUser(user, server)) }
      tablePositions ++ namedUsers
    }
  }

}
