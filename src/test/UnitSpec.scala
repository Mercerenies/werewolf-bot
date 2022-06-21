
package com.mercerenies.werewolf

import id.Id

import org.scalatest.*
import org.scalatest.concurrent.*
import org.scalatestplus.mockito.*
import org.mockito.{Mockito, ArgumentMatchers}
import flatspec.*
import matchers.*

import org.javacord.api.entity.DiscordEntity

abstract class UnitSpec extends AnyFlatSpec with should.Matchers with Futures with MockitoSugar {
  export Mockito.*
  export ArgumentMatchers.*
  export ScalaFutures.*

  def anyId[A <: DiscordEntity](): Id[A] =
    Id.fromLong(anyLong())

}
