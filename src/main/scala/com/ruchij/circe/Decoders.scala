package com.ruchij.circe

import com.ruchij.types.Parser
import enumeratum.{Enum, EnumEntry}
import io.circe.Decoder
import org.joda.time.DateTime

import scala.util.Try

object Decoders {
  implicit val dateTimeDecoder: Decoder[DateTime] =
    Decoder.decodeString.emapTry(dateTimeString => Try(DateTime.parse(dateTimeString)))

  implicit val stringDecoder: Decoder[String] =
    Decoder.decodeString.emap(value => if (value.isEmpty) Left("cannot be empty") else Right(value))

  implicit def enumDecoder[A <: EnumEntry](implicit enumValue: Enum[A]): Decoder[A] =
    Decoder.decodeString.emap { input => enumValue.withNameInsensitiveEither(input).left.map(_.getMessage()) }

  implicit def valueClassDecoder[A <: AnyVal, B](
    implicit parser: Parser[B, A],
    decoder: Decoder[B]
  ): Decoder[A] =
    decoder.emap(value => parser.parse(value).left.map(_.head))

}
