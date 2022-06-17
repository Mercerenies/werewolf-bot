
package com.mercerenies.werewolf
package util
package html

import scala.util.matching.Regex

// Precondition: attributeName shall be nonempty and shall contain
// only alphabetical characters.
case class Attribute(val attributeName: String, val attributeValue: String) {

  if (!Attribute.ATTRIBUTE_NAME_VALIDATOR.matches(attributeName)) {
    throw new IllegalArgumentException(s"Bad attribute name ${attributeName}")
  }

  def mkString: String =
    attributeName + "=\"" + HtmlBuilder.escape(attributeValue) + "\""

}

object Attribute {

  private val ATTRIBUTE_NAME_VALIDATOR: Regex =
    raw"^[A-Za-z]+$$".r

  extension (attrName: String)
    def :=(value: String): Attribute =
      Attribute(attrName, value)

}
