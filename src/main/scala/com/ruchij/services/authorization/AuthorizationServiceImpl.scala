package com.ruchij.services.authorization

import cats.implicits._
import cats.{MonadError, ~>}
import com.ruchij.daos.permission.PermissionDao
import com.ruchij.daos.permission.models.PermissionType
import com.ruchij.exceptions.AuthorizationException
import com.ruchij.syntax._

class AuthorizationServiceImpl[F[_]: MonadError[*[_], Throwable], G[_]](permissionDao: PermissionDao[G])(
  implicit transaction: G ~> F
) extends AuthorizationService[F] {

  override def withPermission[A](requesterId: String, resourceId: String, permissionType: PermissionType)(
    block: => F[A]
  ): F[A] =
    transaction(permissionDao.findBy(requesterId, requesterId, permissionType))
      .flatMap {
        _.toF[Throwable, F](AuthorizationException(s"$requesterId does not $permissionType permission to $resourceId"))
      }
      .productR(block)

}
