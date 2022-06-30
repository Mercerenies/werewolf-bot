
package com.mercerenies.werewolf
package game
package source

sealed trait Inspiration {
  def description: String
}

object Inspiration {

  case class LiftedFrom(val sourceMaterial: SourceMaterial) extends Inspiration {

    override def description: String =
      s"From ${sourceMaterial.name}"

  }

  case class InspiredBy(val originalRoleName: String, val sourceMaterial: SourceMaterial) extends Inspiration {

    override def description: String =
      s"Inspired by ${originalRoleName} in ${sourceMaterial.name}"

  }

  case object Custom extends Inspiration {

    override val description: String =
      "Custom role"

  }

}
