package com.ruchij.daos.doobie

import enumeratum.{Enum, EnumEntry}
import org.{h2, postgresql}

import java.sql.Driver
import scala.reflect.ClassTag
import scala.util.matching.Regex

sealed abstract class DatabaseDriver[A <: Driver](implicit classTag: ClassTag[A]) extends EnumEntry {
  val name: String

  val clazz: Class[_] = classTag.runtimeClass
}

object DatabaseDriver extends Enum[DatabaseDriver[_]] {
  val DatabaseType: Regex = "jdbc:([^:]+):.*".r

  case object H2 extends DatabaseDriver[h2.Driver] {
    override val name: String = "h2"
  }

  case object Postgresql extends DatabaseDriver[postgresql.Driver] {
    override val name: String = "postgresql"
  }

  override def values: IndexedSeq[DatabaseDriver[_]] = findValues

  val infer: String => Option[DatabaseDriver[_]] = {
    case DatabaseType(name) => values.find(_.name.equalsIgnoreCase(name))

    case value => None
  }
}
