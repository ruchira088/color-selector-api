package com.ruchij.services.authorization

import cats.effect.Clock
import cats.implicits._
import cats.{MonadError, ~>}
import com.ruchij.daos.permission.PermissionDao
import com.ruchij.daos.permission.models.{Permission, PermissionType}
import com.ruchij.exceptions.{AuthorizationException, ResourceConflictException}
import com.ruchij.services.models.AuthenticatedContext
import com.ruchij.syntax._
import org.joda.time.DateTime

import java.util.concurrent.TimeUnit

class AuthorizationServiceImpl[F[_]: MonadError[*[_], Throwable]: Clock, G[_]](permissionDao: PermissionDao[G])(
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

  override def grantPermission(userId: String, resourceId: String, permissionType: PermissionType)(
    implicit authenticatedContext: AuthenticatedContext
  ): F[Permission] =
    withPermission(authenticatedContext.user.id, resourceId, PermissionType.Write) {
      for {
        _ <-
          transaction(permissionDao.findBy(userId, resourceId, permissionType))
            .flatMap {
              _.toF[Throwable, F](ResourceConflictException(s"$userId already has $permissionType permission to $resourceId"))
            }

        timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

        permission = Permission(userId, timestamp, resourceId, permissionType)

        _ <- transaction(permissionDao.insert(permission))
      } yield permission
    }
}
