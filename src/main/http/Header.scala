
package com.mercerenies.werewolf
package http

case class Header(val list: List[(String, String)]) {

  def toList: List[(String, String)] =
    list

}

object Header {

  def apply(args: (String, String)*) =
    new Header(List(args: _*))

}
