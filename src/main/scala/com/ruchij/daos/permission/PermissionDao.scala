package com.ruchij.daos.permission

import com.ruchij.daos.permission.models.{Permission, PermissionType}

import java.util.UUID

trait PermissionDao[F[_]] {

  def insert(permission: Permission): F[Int]

  def findBy(requesterId: UUID, resourceId: UUID, permissionType: PermissionType): F[Option[Permission]]

}
