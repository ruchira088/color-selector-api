package com.ruchij.migration

import cats.effect.{ExitCode, IO, IOApp, Sync}
import cats.implicits._
import com.ruchij.migration.config.DatabaseConfiguration
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import pureconfig.ConfigSource

object MigrationApp extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    for {
      configObjectSource <- IO.delay(ConfigSource.defaultApplication)
      databaseConfiguration <- DatabaseConfiguration.load[IO](configObjectSource)

      _ <- IO.delay(println("Migration application started"))

      migrationResult <- migrate[IO](databaseConfiguration)

      _ <- IO.delay(println(s"Migration completed"))
    }
    yield ExitCode.Success


  def migrate[F[_]: Sync](databaseConfiguration: DatabaseConfiguration): F[MigrateResult] =
    for {
      flyway <-
        Sync[F].delay {
          Flyway.configure()
            .dataSource(databaseConfiguration.url, databaseConfiguration.user, databaseConfiguration.password)
            .load()
        }

      result <- Sync[F].delay(flyway.migrate())
    }
    yield result


}
