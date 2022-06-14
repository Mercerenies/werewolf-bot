
package com.mercerenies.werewolf

import id.Id

import org.scalatest.*
import org.scalatestplus.mockito.*
import org.mockito.{Mockito, ArgumentMatchers}
import flatspec.*
import matchers.*

import org.javacord.api.entity.DiscordEntity

abstract class UnitSpec extends AnyFlatSpec with should.Matchers with MockitoSugar {
  export Mockito.*
  export ArgumentMatchers.*

  def anyId[A <: DiscordEntity](): Id[A] =
    Id.fromLong(anyLong())

}
