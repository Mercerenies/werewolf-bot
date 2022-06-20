
package com.mercerenies.werewolf
package http

enum RequestMethod(override val toString: String) {
  case Get extends RequestMethod("GET")
  case Post extends RequestMethod("POST")
}
