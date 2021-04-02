package com.ruchij.services.authorization

import com.ruchij.daos.permission.models.PermissionType

trait AuthorizationService[F[_]] {

  def withPermission[A](requesterId: String, resourceId: String, permissionType: PermissionType)(block: => F[A]): F[A]

}
