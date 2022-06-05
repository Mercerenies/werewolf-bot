
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity

final class StatelessRoleInstance(
  override val role: Role,
)(
  using isValid: (StatelessRoleInstance <:< role.Instance),
) extends RoleInstance {

  val coherenceProof: this.type <:< this.role.Instance =
    summon[this.type <:< StatelessRoleInstance] andThen isValid

}
