
package com.mercerenies.werewolf
package game
package record

import id.{UserMapping, Id}
import board.{TablePosition, Position}
import board.snapshot.{BoardSnapshot, RoleSnapshot}
import util.html.{HtmlFragment, HtmlBuilder}
import parser.assignment.{NamedUser, NamedPosition}
import role.Role

import org.javacord.api.entity.user.User

// A record which details an action performed by a particular player
// and the feedback they received from it.
class ActionPerformedRecord(
  private val subjectRole: RoleSnapshot,
  private val subject: Id[User],
)(
  private val builder: (HtmlFragment, ActionPerformedRecord.ImplicitUserMapping) ?=> Unit,
) extends GameRecord {
  import ActionPerformedRecord.*

  private def sentence(using HtmlFragment, ImplicitUserMapping): Unit = {
    t("The ")
    roleName(subjectRole)
    t(" (")
    playerName(subject)
    t(") ")
    builder
    t(".")
  }

  def displayText(userMapping: UserMapping): String = {
    given ImplicitUserMapping = ImplicitUserMapping(userMapping)
    beginPlaintext {
      sentence
    }
  }

  def htmlText(userMapping: UserMapping)(using HtmlFragment): Unit = {
    given ImplicitUserMapping = ImplicitUserMapping(userMapping)
    li {
      sentence
    }
  }

  override def mapActor(fn: (RoleSnapshot) => RoleSnapshot): ActionPerformedRecord =
    ActionPerformedRecord(fn(subjectRole), subject)(builder)

}

object ActionPerformedRecord {

  // The ActionPerformedRecord singleton also exports everything from
  // HtmlBuilder so that users of this DSL can simply import
  // ActionPerformedRecord.* and get everything they need.
  export HtmlBuilder.*

  // Isomorphic to UserMapping, but it's a separate type in order to
  // clarify that we *intend* to use this one for implicit resolution.
  final class ImplicitUserMapping(val userMapping: UserMapping) {
    export userMapping.*
  }

  def nameOf(id: Id[User])(using m: ImplicitUserMapping): String =
    m.nameOf(id)

  def roleName(role: RoleSnapshot)(using HtmlFragment, ImplicitUserMapping): Unit =
    b { t(role.name) }

  def playerName(id: Id[User])(using HtmlFragment, ImplicitUserMapping): Unit =
    b { t(nameOf(id)) }

  def position(id: Id[User])(using HtmlFragment, ImplicitUserMapping): Unit =
    playerName(id)

  def roleName(role: Role)(using HtmlFragment, ImplicitUserMapping): Unit =
    b { t(role.name) }

  def position(pos: TablePosition)(using HtmlFragment, ImplicitUserMapping): Unit =
    b { t(pos.name) }

  def position(pos: Position)(using HtmlFragment, ImplicitUserMapping): Unit =
    pos match {
      case Position.Table(p) => position(p)
      case Position.Player(p) => position(p)
    }

}
