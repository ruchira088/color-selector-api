package com.ruchij.daos.user.models

import cats.data.NonEmptyList
import com.ruchij.types.Parser
import org.apache.commons.validator.routines.EmailValidator

case class Email(value: String) extends AnyVal

object Email {
  private val emailValidator = EmailValidator.getInstance()

  implicit val emailParser: Parser[String, Email] =
    (input: String) => from(input).toRight(NonEmptyList.of(s"""Unable to parse "$input" as an email"""))

  def from(value: String): Option[Email] =
    Option.when(emailValidator.isValid(value))(Email(value))
}
