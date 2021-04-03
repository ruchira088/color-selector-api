package com.ruchij.services.color

import cats.effect.Clock
import cats.implicits._
import cats.{MonadError, ~>}
import com.ruchij.daos.color.ColorDao
import com.ruchij.daos.color.models.{Color, ColorValue}
import com.ruchij.daos.permission.models.PermissionType
import com.ruchij.exceptions.ResourceConflictException
import com.ruchij.services.authorization.AuthorizationService
import com.ruchij.services.models.AuthenticatedContext
import com.ruchij.syntax._
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class ColorServiceImpl[F[_]: MonadError[*[_], Throwable]: Clock, G[_]](
  authorizationService: AuthorizationService[F],
  colorDao: ColorDao[G]
)(implicit transaction: G ~> F)
    extends ColorService[F] {

  override def insert(userId: UUID, colorValue: ColorValue)(implicit context: AuthenticatedContext): F[Color] =
    authorizationService.withPermission(context.user.id, userId, PermissionType.Write) {
      for {
        _ <- transaction(colorDao.findByUserId(userId))
          .flatMap(_.nonEmptyF[Throwable, F](ResourceConflictException(s"$userId has already selected a color")))

        timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

        color = Color(userId, timestamp, timestamp, colorValue)

        _ <- transaction(colorDao.insert(color))
      } yield color
    }

  override def findByUserId(userId: UUID)(implicit context: AuthenticatedContext): F[List[Color]] =
    authorizationService.withPermission(context.user.id, userId, PermissionType.Read) {
      transaction(colorDao.findByUserId(userId)).map(_.toList)
    }

}
