package com.ruchij.test

import cats.effect.{Blocker, Concurrent, ContextShift, Resource, Timer}
import com.ruchij.App
import com.ruchij.config.{AuthenticationConfiguration, BuildInformation, HttpConfiguration, ServiceConfiguration}
import com.ruchij.migration.MigrationApp
import com.ruchij.migration.config.DatabaseConfiguration
import com.ruchij.types.RandomGenerator
import org.http4s.HttpApp

import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

object HttpTestApp {
  val BuildInfo: BuildInformation = BuildInformation(Some("test-branch"), Some("my-commit"), None)

  val AuthConfig: AuthenticationConfiguration = AuthenticationConfiguration(60 seconds)

  val HttpConfig: HttpConfiguration = HttpConfiguration("127.0.0.0", 80)

  def databaseConfiguration(suffix: String): DatabaseConfiguration =
    DatabaseConfiguration(
      s"jdbc:h2:mem:color-selector-$suffix;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
      "",
      ""
    )

  def apply[F[_]: Concurrent: Timer: ContextShift: RandomGenerator[*[_], UUID]](
    implicit executionContext: ExecutionContext
  ): Resource[F, (HttpApp[F], ServiceConfiguration)] = {
    val blocker = Blocker.liftExecutionContext(executionContext)

    Resource.eval(RandomGenerator[F, UUID].generate)
      .map { uuid =>
        ServiceConfiguration(databaseConfiguration(uuid.toString), AuthConfig, HttpConfig, BuildInfo)
      }
      .evalTap {
        serviceConfiguration => MigrationApp.migrate(serviceConfiguration.databaseConfiguration)
      }
      .flatMap { serviceConfiguration =>
        App
          .build(serviceConfiguration, blocker, blocker)
          .map(_ -> serviceConfiguration)
      }
  }
}
