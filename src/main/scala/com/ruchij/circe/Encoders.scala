package com.ruchij.circe

import enumeratum.EnumEntry
import io.circe.Encoder
import org.joda.time.DateTime
import shapeless.{::, Generic, HNil}

object Encoders {
  implicit val dateTimeEncoder: Encoder[DateTime] = Encoder.encodeString.contramap[DateTime](_.toString)

  implicit def enumEncoder[A <: EnumEntry]: Encoder[A] = Encoder.encodeString.contramap[A](_.entryName)

  implicit def stringValueClassEncoder[A <: AnyVal](implicit generic: Generic.Aux[A, String :: HNil]): Encoder[A] =
    valueClassEncoder[A, String]

  def valueClassEncoder[A <: AnyVal, B](implicit generic: Generic.Aux[A, B :: HNil], encoder: Encoder[B]): Encoder[A] =
    encoder.contramap[A](value => generic.to(value).head)
}
