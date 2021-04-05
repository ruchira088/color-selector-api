package com.ruchij.daos.doobie

import doobie.implicits.javasql.TimestampMeta
import doobie.util.Get
import enumeratum.{Enum, EnumEntry}
import org.joda.time.DateTime

import java.sql.Timestamp
import java.util.UUID
import scala.util.Try

object GetMappings {

  implicit def enumGet[A <: EnumEntry](implicit enumValue: Enum[A]): Get[A] =
    Get[String].temap[A] {
      value => enumValue.withNameInsensitiveEither(value).left.map(_.getMessage())
    }

  implicit val dateTimeGet: Get[DateTime] = Get[Timestamp].map { date => new DateTime(date.getTime) }

  implicit val uuidGet: Get[UUID] =
    Get[String].temap { value => Try(UUID.fromString(value)).toEither.left.map(_.getMessage) }

}
