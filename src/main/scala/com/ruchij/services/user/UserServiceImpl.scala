package com.ruchij.services.user

import cats.effect.Clock
import cats.implicits._
import cats.{Monad, MonadError, ~>}
import com.ruchij.daos.credentials.CredentialsDao
import com.ruchij.daos.credentials.models.Credentials
import com.ruchij.daos.user.UserDao
import com.ruchij.daos.user.models.User
import com.ruchij.exceptions.ResourceConflictException
import com.ruchij.services.hash.password.PasswordHashService
import com.ruchij.syntax._
import com.ruchij.types.RandomGenerator
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class UserServiceImpl[F[_]: Monad: Clock: RandomGenerator[*[_], UUID], G[_]: MonadError[*[_], Throwable]](
  passwordHashService: PasswordHashService[F],
  userDao: UserDao[G],
  credentialsDao: CredentialsDao[G]
)(implicit transaction: G ~> F)
    extends UserService[F] {

  override def create(username: String, password: String, firstName: String, lastName: String, email: String): F[User] =
    for {
      _ <- transaction {
        userDao
          .findByUsername(username)
          .flatMap(_.nonEmptyF[Throwable, G](ResourceConflictException(s"$username already exists")))
          .product {
            userDao.findByEmail(email)
              .flatMap {
                _.nonEmptyF[Throwable, G] {
                  ResourceConflictException(s"$email already has a registered account")
                }
              }
          }
      }

      userId <- RandomGenerator[F, UUID].generate.map(_.toString)

      hashedPassword <- passwordHashService.hash(password)

      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

      user = User(userId, timestamp, timestamp, username, firstName, lastName, email)
      credentials = Credentials(userId, timestamp, timestamp, hashedPassword)

      _ <- transaction { userDao.insert(user).productR(credentialsDao.insert(credentials)) }

    } yield user

  override def usernameAvailable(username: String): F[Boolean] =
    transaction(userDao.findByUsername(username)).map(_.isEmpty)
}
