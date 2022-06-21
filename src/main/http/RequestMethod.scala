
package com.mercerenies.werewolf
package http

enum RequestMethod(override val toString: String) {
  case GET extends RequestMethod("GET")
  case POST extends RequestMethod("POST")
}
