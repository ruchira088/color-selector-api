package com.ruchij.daos.permission

import com.ruchij.daos.permission.models.{Permission, PermissionType}

trait PermissionDao[F[_]] {

  def insert(permission: Permission): F[Int]

  def findBy(requesterId: String, resourceId: String, permissionType: PermissionType): F[Option[Permission]]

}
