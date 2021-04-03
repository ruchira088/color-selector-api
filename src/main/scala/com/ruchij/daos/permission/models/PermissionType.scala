package com.ruchij.daos.permission.models

import cats.data.NonEmptyList
import enumeratum.{Enum, EnumEntry}

sealed trait PermissionType extends EnumEntry {
  val all: NonEmptyList[PermissionType]
}

object PermissionType extends Enum[PermissionType] {
  case object Write extends PermissionType {
    override val all: NonEmptyList[PermissionType] = NonEmptyList.of(Write, Read)
  }

  case object Read extends PermissionType {
    override val all: NonEmptyList[PermissionType] = NonEmptyList.of(Read)
  }

  override def values: IndexedSeq[PermissionType] = findValues
}
