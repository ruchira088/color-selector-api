package com.ruchij.services.user

import cats.effect.Clock
import cats.implicits._
import cats.{Monad, MonadError, ~>}
import com.ruchij.daos.credentials.CredentialsDao
import com.ruchij.daos.credentials.models.Credentials
import com.ruchij.daos.permission.PermissionDao
import com.ruchij.daos.permission.models.{Permission, PermissionType}
import com.ruchij.daos.user.UserDao
import com.ruchij.daos.user.models.{Email, User}
import com.ruchij.exceptions.ResourceConflictException
import com.ruchij.services.hash.password.PasswordHashingService
import com.ruchij.syntax._
import com.ruchij.types.RandomGenerator
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class UserServiceImpl[F[_]: Monad: Clock: RandomGenerator[*[_], UUID], G[_]: MonadError[*[_], Throwable]](
  passwordHashService: PasswordHashingService[F],
  userDao: UserDao[G],
  credentialsDao: CredentialsDao[G],
  permissionDao: PermissionDao[G]
)(implicit transaction: G ~> F)
    extends UserService[F] {

  override def create(username: String, password: String, firstName: String, lastName: String, email: Email): F[User] =
    for {
      _ <- transaction {
        userDao
          .findByUsername(username)
          .flatMap(_.nonEmptyF[Throwable, G](ResourceConflictException(s"$username already exists")))
          .product {
            userDao
              .findByEmail(email)
              .flatMap {
                _.nonEmptyF[Throwable, G] {
                  ResourceConflictException(s"${email.value} already has a registered account")
                }
              }
          }
      }

      userId <- RandomGenerator[F, UUID].generate

      hashedPassword <- passwordHashService.hash(password)

      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

      user = User(userId, timestamp, timestamp, username, firstName, lastName, email)
      credentials = Credentials(userId, timestamp, timestamp, hashedPassword)
      permission = Permission(userId, timestamp, userId, PermissionType.Write)

      _ <- transaction {
        userDao
          .insert(user)
          .productR(credentialsDao.insert(credentials))
          .productR(permissionDao.insert(permission))
      }

    } yield user

  override def retrieveAll(username: Option[String], offset: Int, pageSize: Int): F[List[User]] =
    transaction(userDao.search(username, offset, pageSize))
}
