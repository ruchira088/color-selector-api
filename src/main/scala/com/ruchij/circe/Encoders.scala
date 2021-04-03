package com.ruchij.circe

import com.ruchij.daos.color.models.ColorValue
import io.circe.Encoder
import org.joda.time.DateTime

object Encoders {
  implicit val dateTimeEncoder: Encoder[DateTime] = Encoder.encodeString.contramap[DateTime](_.toString)

  implicit val colorValueEncoder: Encoder[ColorValue] = Encoder.encodeString.contramap[ColorValue](_.value)

  implicit def throwableEncoder[A <: Throwable]: Encoder[A] =
    Encoder.encodeString.contramap[A](_.getMessage)
}
