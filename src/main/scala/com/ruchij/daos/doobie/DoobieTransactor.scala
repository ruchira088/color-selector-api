package com.ruchij.daos.doobie

import cats.effect.{Async, Blocker, ContextShift}
import cats.implicits._
import com.ruchij.migration.config.DatabaseConfiguration
import com.ruchij.syntax._
import doobie.util.transactor.Transactor

object DoobieTransactor {

  def create[F[_]: Async: ContextShift](
    databaseConfiguration: DatabaseConfiguration,
    ioBlocker: Blocker
  ): F[Transactor.Aux[F, Unit]] =
    DatabaseDriver
      .infer(databaseConfiguration.url)
      .toF[Throwable, F] {
        new IllegalArgumentException(s"Unable to infer database driver from ${databaseConfiguration.url}")
      }
      .map { databaseDriver =>
        Transactor.fromDriverManager(
          databaseDriver.clazz.getName,
          databaseConfiguration.url,
          databaseConfiguration.user,
          databaseConfiguration.password,
          ioBlocker
        )
      }

}
