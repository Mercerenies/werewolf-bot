
package com.mercerenies.werewolf
package game
package role

import name.NamedEntity

final class StatelessRoleInstance[R <: Role](
  override val role: R,
) extends RoleInstance[R]
