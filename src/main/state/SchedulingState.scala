
package com.mercerenies.werewolf
package state

import id.Id
import command.CommandResponse
import manager.GamesManager
import properties.GameProperties
import timer.Cancellable

import org.javacord.api.entity.Nameable
import org.javacord.api.entity.user.User
import org.javacord.api.entity.channel.TextChannel
import org.javacord.api.entity.message.Message
import org.javacord.api.interaction.SlashCommandInteraction

import java.util.concurrent.LinkedBlockingQueue

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*

// Mixin for game states which need to schedule events on the
// GamesManager's timer instance. When cancelAll() is called, all
// scheduled events are cancelled. This also occurs automatically when
// the onExitState event is triggered.
transparent trait SchedulingState extends GameState {

  private val eventQueue: LinkedBlockingQueue[Cancellable] = LinkedBlockingQueue()

  def schedule(mgr: GamesManager, delay: Duration)(task: Runnable): Cancellable = {
    val cancellable = mgr.timer.scheduleTaskCast(delay)(task)
    eventQueue.add(cancellable)
    cancellable
  }

  def cancelAll(): Unit = {
    // Drain it to a local buffer we control in one step (so we don't
    // have to worry about race conditions), and then cancel each one
    // in sequence.
    val buffer = ArrayBuffer[Cancellable]()
    eventQueue.drainTo(buffer.asJava)
    buffer.foreach { _.cancel() }
  }

  override def onExitState(mgr: GamesManager): Unit = {
    super.onExitState(mgr)
    cancelAll()
  }

}
