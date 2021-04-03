package com.ruchij.services.authorization

import com.ruchij.daos.permission.models.{Permission, PermissionType}
import com.ruchij.services.models.AuthenticatedContext

import java.util.UUID

trait AuthorizationService[F[_]] {

  def withPermission[A](requesterId: UUID, resourceId: UUID, permissionType: PermissionType)(block: => F[A]): F[A]

  def grantPermission(userId: UUID, resourceId: UUID, permissionType: PermissionType)(implicit authenticatedContext: AuthenticatedContext): F[Permission]

}
