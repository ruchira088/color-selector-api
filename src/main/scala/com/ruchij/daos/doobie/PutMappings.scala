package com.ruchij.daos.doobie

import doobie.util.Put
import doobie.implicits.javasql.TimestampMeta
import enumeratum.EnumEntry
import org.joda.time.DateTime
import org.tpolecat.typename.TypeName

import java.sql.Timestamp
import java.util.{Date, UUID}

object PutMappings {

  implicit def enumPut[A <: EnumEntry: TypeName]: Put[A] = Put[String].tcontramap[A](_.entryName)

  implicit val dateTimePut: Put[DateTime] = Put[Timestamp].tcontramap(dateTime => new Timestamp(dateTime.getMillis))

  implicit val uuidPut: Put[UUID] = Put[String].tcontramap[UUID](_.toString)

}
