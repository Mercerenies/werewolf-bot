
package com.mercerenies.werewolf

import org.scalatest.*
import org.scalatestplus.mockito.*
import org.mockito.{Mockito, ArgumentMatchers}
import flatspec.*
import matchers.*

abstract class UnitSpec extends AnyFlatSpec with should.Matchers with MockitoSugar {
  export Mockito.*
  export ArgumentMatchers.*
}
