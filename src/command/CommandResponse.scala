
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

  def simple(message: String): CommandResponse[Unit] =
    CommandResponse(message, Nil) { _ => () }

  def ephemeral(message: String): CommandResponse[Unit] =
    CommandResponse(message, List(InteractionCallbackDataFlag.EPHEMERAL)) { _ => () }

}
