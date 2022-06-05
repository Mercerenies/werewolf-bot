
package com.mercerenies.werewolf
package game
package board

import id.Id

import org.javacord.api.entity.user.User

// A position on the board where a role card shall sit.
enum Position {
  // A card in the middle of the table.
  case Table(val side: board.Table)
  // A card in front of a player.
  case Player(val playerId: Id[User])
}
