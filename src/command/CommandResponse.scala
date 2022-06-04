
package com.mercerenies.werewolf
package command

import org.javacord.api.interaction.callback.{InteractionCallbackDataFlag, InteractionOriginalResponseUpdater}
import org.javacord.api.interaction.SlashCommandInteraction

import scala.jdk.FutureConverters.*
import scala.concurrent.{Future, ExecutionContext}

case class CommandResponse[+R](
  val message: String,
  val responseFlags: List[InteractionCallbackDataFlag],
)(
  val postCallback: (InteractionOriginalResponseUpdater) => R,
) {

  def void: CommandResponse[Unit] =
    this.andThen { _ => () }

  def andThen[B](fn: R => B): CommandResponse[B] =
    CommandResponse(message, responseFlags) { updater => fn(postCallback(updater)) }

  def execute(interaction: SlashCommandInteraction)(using ExecutionContext): Future[R] = {
    val responder = interaction.createImmediateResponder()
    responder.setContent(message)
    if (!responseFlags.isEmpty) {
      responder.setFlags(responseFlags: _*)
    }
    responder.respond().asScala.map(postCallback)
  }

}

object CommandResponse {

  def simple(message: String): CommandResponse[InteractionOriginalResponseUpdater] =
    CommandResponse(message, Nil) { x => x }

  def ephemeral(message: String): CommandResponse[InteractionOriginalResponseUpdater] =
    CommandResponse(message, List(InteractionCallbackDataFlag.EPHEMERAL)) { x => x }

}
