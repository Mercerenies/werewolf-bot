
package com.mercerenies.werewolf
package game
package role
package wincon

// Every player either wins or loses the game. A "soft" win or loss
// can happily share the victory with anyone else. A "hard" win or
// loss automatically counts all of the win conditions with lower
// precedence as losses, regardless of whether the condition is
// actually met.
enum Outcome {
  case HardLoss // <-- Probably not used, but here for consistency
  case SoftLoss
  case SoftWin
  case HardWin

  def isHard: Boolean =
    (this == HardLoss) || (this == HardWin)

  def isWin: Boolean =
    (this == SoftWin) || (this == HardWin)

  def isSoft: Boolean =
    !isHard

  def isLoss: Boolean =
    !isWin

}

object Outcome {

  // Always a soft outcome
  def softWin(b: Boolean) =
    if (b) { Outcome.SoftWin } else { Outcome.SoftLoss }

  // Hard outcome if victorious, soft if failure
  def hardWin(b: Boolean) =
    if (b) { Outcome.HardWin } else { Outcome.SoftLoss }

}
