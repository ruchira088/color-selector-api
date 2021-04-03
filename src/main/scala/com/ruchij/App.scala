package com.ruchij

import cats.effect.{Async, Blocker, Clock, ContextShift, ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits._
import com.ruchij.config.ServiceConfiguration
import com.ruchij.daos.authentication.DoobieAuthenticationTokenDao
import com.ruchij.daos.color.DoobieColorDao
import com.ruchij.daos.credentials.DoobieCredentialsDao
import com.ruchij.daos.doobie.DoobieTransactor
import com.ruchij.daos.permission.DoobiePermissionDao
import com.ruchij.daos.user.DoobieUserDao
import com.ruchij.migration.MigrationApp
import com.ruchij.services.authentication.AuthenticationServiceImpl
import com.ruchij.services.authorization.AuthorizationServiceImpl
import com.ruchij.services.color.ColorServiceImpl
import com.ruchij.services.hash.password.BCryptPasswordHashingService
import com.ruchij.services.health.HealthServiceImpl
import com.ruchij.services.user.UserServiceImpl
import com.ruchij.types.{FunctionKTypes, RandomGenerator}
import com.ruchij.web.Routes
import doobie.ConnectionIO
import org.http4s.HttpApp
import org.http4s.server.blaze.BlazeServerBuilder
import pureconfig.ConfigSource

import java.util.UUID
import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      serviceConfiguration <- ServiceConfiguration.parse[IO](configObjectSource)

      _ <-
        build[IO](serviceConfiguration).use { httpApp =>
          BlazeServerBuilder
            .apply[IO](ExecutionContext.global)
            .withHttpApp(httpApp)
            .bindHttp(serviceConfiguration.httpConfiguration.port, serviceConfiguration.httpConfiguration.host)
            .serve
            .compile
            .drain
        }
    } yield ExitCode.Success

  def build[F[_]: Async: ContextShift: Clock: RandomGenerator[*[_], UUID]](
    serviceConfiguration: ServiceConfiguration
  ): Resource[F, HttpApp[F]] =
    for {
      cpuCount <- Resource.eval(Sync[F].delay(Runtime.getRuntime.availableProcessors()))
      cpuBlockingThreadPool <- Resource.eval(Sync[F].delay(Executors.newFixedThreadPool(cpuCount)))
      cpuBlocker = Blocker.liftExecutorService(cpuBlockingThreadPool)

      ioBlockingThreadPool <- Resource.eval(Sync[F].delay(Executors.newCachedThreadPool()))
      ioBlocker = Blocker.liftExecutorService(ioBlockingThreadPool)

      httpApp <- Resource.eval(build[F](serviceConfiguration, cpuBlocker, ioBlocker))
    }
    yield httpApp

  def build[F[_]: Async: ContextShift: Clock: RandomGenerator[*[_], UUID]](
    serviceConfiguration: ServiceConfiguration,
    cpuBlocker: Blocker,
    ioBlocker: Blocker
  ): F[HttpApp[F]] = {
    MigrationApp.migrate[F](serviceConfiguration.databaseConfiguration)
      .productR {
        DoobieTransactor
          .create[F](serviceConfiguration.databaseConfiguration, ioBlocker)
          .map(transactor => FunctionKTypes.connectionIoToF[F](transactor))
          .map { implicit transaction =>
            val healthService = new HealthServiceImpl[F](serviceConfiguration.buildInformation)
            val passwordHashingService = new BCryptPasswordHashingService[F](cpuBlocker)

            val authorizationService = new AuthorizationServiceImpl[F, ConnectionIO](DoobiePermissionDao)
            val authenticationService =
              new AuthenticationServiceImpl[F, ConnectionIO](
                passwordHashingService,
                DoobieAuthenticationTokenDao,
                DoobieUserDao,
                DoobieCredentialsDao,
                serviceConfiguration.authenticationConfiguration
              )

            val userService =
              new UserServiceImpl[F, ConnectionIO](
                passwordHashingService,
                DoobieUserDao,
                DoobieCredentialsDao,
                DoobiePermissionDao
              )

            val colorService =
              new ColorServiceImpl[F, ConnectionIO](authorizationService, DoobieColorDao)

            Routes(userService, colorService, authorizationService, authenticationService, healthService)
          }
      }
  }
}
