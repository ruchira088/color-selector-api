package com.ruchij.daos.permission.models

import enumeratum.{Enum, EnumEntry}

sealed trait PermissionType extends EnumEntry

object PermissionType extends Enum[PermissionType] {
  case object Write extends PermissionType
  case object Read extends PermissionType

  override def values: IndexedSeq[PermissionType] = findValues
}
