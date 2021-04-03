package com.ruchij.services.authorization

import com.ruchij.daos.permission.models.{Permission, PermissionType}
import com.ruchij.services.models.AuthenticatedContext

trait AuthorizationService[F[_]] {

  def withPermission[A](requesterId: String, resourceId: String, permissionType: PermissionType)(block: => F[A]): F[A]

  def grantPermission(userId: String, resourceId: String, permissionType: PermissionType)(implicit authenticatedContext: AuthenticatedContext): F[Permission]

}
