package com.ruchij.services.authentication

import cats.effect.Clock
import cats.implicits._
import cats.{Applicative, ApplicativeError, Monad, MonadError, ~>}
import com.ruchij.config.AuthenticationConfiguration
import com.ruchij.daos.authentication.AuthenticationTokenDao
import com.ruchij.daos.authentication.models.AuthenticationToken
import com.ruchij.daos.credentials.CredentialsDao
import com.ruchij.daos.credentials.models.Credentials
import com.ruchij.daos.user.UserDao
import com.ruchij.daos.user.models.User
import com.ruchij.exceptions.{AuthenticationException, ResourceNotFoundException}
import com.ruchij.services.hash.password.PasswordHashingService
import com.ruchij.syntax._
import com.ruchij.types.RandomGenerator
import org.joda.time.DateTime

import java.util.UUID
import java.util.concurrent.TimeUnit

class AuthenticationServiceImpl[F[_]: MonadError[*[_], Throwable]: Clock: RandomGenerator[*[_], UUID], G[_]: Monad](
  passwordHashService: PasswordHashingService[F],
  authenticationTokenDao: AuthenticationTokenDao[G],
  userDao: UserDao[G],
  credentialsDao: CredentialsDao[G],
  authenticationConfiguration: AuthenticationConfiguration
)(implicit transaction: G ~> F)
    extends AuthenticationService[F] {

  override def login(username: String, password: String): F[AuthenticationToken] =
    for {
      (user, credentials) <- transaction {
        userDao
          .findByUsername(username)
          .flatMap { maybeUser =>
            maybeUser.fold[G[Option[(User, Credentials)]]](Applicative[G].pure(None)) { user =>
              credentialsDao.findByUserId(user.id).map(_.map(user -> _))
            }
          }
      }.flatMap(_.toF[Throwable, F](AuthenticationException("Username not found")))

      validPassword <- passwordHashService.checkPassword(password, credentials.saltedHashedPassword)

      _ <- if (validPassword) Applicative[F].unit
      else ApplicativeError[F, Throwable].raiseError(AuthenticationException("Invalid credentials"))

      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

      secret <- RandomGenerator[F, UUID].generate.map(_.toString)

      authenticationToken = AuthenticationToken(
        user.id,
        timestamp,
        timestamp,
        secret,
        timestamp.plus(authenticationConfiguration.sessionDuration.toMillis),
        0
      )

      _ <- transaction(authenticationTokenDao.insert(authenticationToken))
    } yield authenticationToken

  override def authenticate(userId: String, secret: String): F[User] =
    for {
      authenticationToken <- transaction(authenticationTokenDao.find(userId, secret))
        .flatMap(_.toF[Throwable, F](AuthenticationException("Invalid authentication credentials")))

      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

      _ <- if (authenticationToken.expiresAt.isAfter(timestamp)) Applicative[F].unit
      else ApplicativeError[F, Throwable].raiseError(AuthenticationException("Authentication token is expired"))

      user <- transaction {
        authenticationTokenDao
          .update {
            authenticationToken
              .copy(
                renewals = authenticationToken.renewals + 1,
                modifiedAt = timestamp,
                expiresAt = timestamp.plus(authenticationConfiguration.sessionDuration.toMillis)
              )
          }
          .productR(userDao.findByUserId(authenticationToken.userId))
      }.flatMap(_.toF[Throwable, F](ResourceNotFoundException("User not found")))
    } yield user

  override def logout(userId: String, secret: String): F[AuthenticationToken] =
    for {
      timestamp <- Clock[F].realTime(TimeUnit.MILLISECONDS).map(milliseconds => new DateTime(milliseconds))

      authenticationToken <- transaction(authenticationTokenDao.delete(userId, secret))
        .flatMap(_.toF[Throwable, F](ResourceNotFoundException("Authentication token not found")))

    } yield authenticationToken.copy(expiresAt = timestamp)

}
