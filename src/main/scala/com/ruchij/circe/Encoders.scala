package com.ruchij.circe

import com.ruchij.daos.color.models.ColorValue
import io.circe.Encoder
import org.joda.time.DateTime
import shapeless.{::, Generic, HNil}

object Encoders {
  implicit val dateTimeEncoder: Encoder[DateTime] = Encoder.encodeString.contramap[DateTime](_.toString)

  implicit val colorValueEncoder: Encoder[ColorValue] = Encoder.encodeString.contramap[ColorValue](_.value)

  implicit def stringValueClassEncoder[A <: AnyVal](implicit generic: Generic.Aux[A, String :: HNil]): Encoder[A] =
    valueClassEncoder[A, String]

  def valueClassEncoder[A <: AnyVal, B](implicit generic: Generic.Aux[A, B :: HNil], encoder: Encoder[B]): Encoder[A] =
    encoder.contramap[A](value => generic.to(value).head)
}
