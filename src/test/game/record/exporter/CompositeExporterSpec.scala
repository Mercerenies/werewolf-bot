
package com.mercerenies.werewolf
package game
package record
package exporter

import id.UserMapping
import board.BoardTestUtil.SampleUserMapping

import scala.concurrent.{Future, ExecutionContext}

class CompositeExporterSpec extends UnitSpec {

  private val successfulExporter = NullExporter

  private class FailedExporter(val throwable: Throwable) extends RecordExporter {
    def exportRecord(record: RecordedGameHistory, userMapping: UserMapping)(using ExecutionContext): Future[Unit] =
      Future.failed(throwable)
  }

  "The composite exporter" should "run multiple exporters in parallel" in {
    given ExecutionContext = ExecutionContext.global

    val exporter1: RecordExporter = mock
    val exporter2: RecordExporter = mock
    val exporter3: RecordExporter = mock

    when(exporter1.exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]()))
      .thenReturn(Future.successful(()))
    when(exporter2.exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]()))
      .thenReturn(Future.successful(()))
    when(exporter3.exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]()))
      .thenReturn(Future.successful(()))

    val f = CompositeExporter(List(exporter1, exporter2, exporter3)).exportRecord(RecordedGameHistory.empty, SampleUserMapping(0))
    f.futureValue should be (()) // Ensure that the future succeeds.

    verify(exporter1).exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]())
    verify(exporter2).exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]())
    verify(exporter3).exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]())

  }

  it should "run all exporters even if one fails" in {
    given ExecutionContext = ExecutionContext.global

    val exporter1: RecordExporter = mock
    val exporter2: RecordExporter = FailedExporter(new RuntimeException("exporter2"))
    val exporter3: RecordExporter = mock

    when(exporter1.exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]()))
      .thenReturn(Future.successful(()))
    when(exporter3.exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]()))
      .thenReturn(Future.successful(()))

    val f = CompositeExporter(List(exporter1, exporter2, exporter3)).exportRecord(RecordedGameHistory.empty, SampleUserMapping(0))
    f.failed.futureValue.isInstanceOf[CompositeException] shouldBe (true) // Ensure that the future fails.

    // Both should have still run, even the one after the failure.
    verify(exporter1).exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]())
    verify(exporter3).exportRecord(any[RecordedGameHistory](), any[UserMapping]())(using any[ExecutionContext]())

  }

  it should "collect the exceptions into one broad exception" in {
    given ExecutionContext = ExecutionContext.global

    val exporter1: RecordExporter = successfulExporter
    val exporter2: RecordExporter = FailedExporter(new RuntimeException("exporter2"))
    val exporter3: RecordExporter = FailedExporter(new RuntimeException("exporter3"))

    val f = CompositeExporter(List(exporter1, exporter2, exporter3)).exportRecord(RecordedGameHistory.empty, SampleUserMapping(0))
    val exc = f.failed.futureValue

    exc.isInstanceOf[CompositeException] shouldBe (true)
    val compExc = exc.asInstanceOf[CompositeException]

    compExc.errors should have length (2)
    compExc.errors(0).isInstanceOf[RuntimeException] should be (true)
    compExc.errors(1).isInstanceOf[RuntimeException] should be (true)
    compExc.errors(0).getMessage should be ("exporter2")
    compExc.errors(1).getMessage should be ("exporter3")

  }

}
