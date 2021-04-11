package com.ruchij.daos.doobie

import cats.effect.{Async, Blocker, ContextShift, Resource}
import com.ruchij.migration.config.DatabaseConfiguration
import com.ruchij.syntax._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

object DoobieTransactor {

  def create[F[_]: Async: ContextShift](
    databaseConfiguration: DatabaseConfiguration,
    ioBlocker: Blocker
  ): Resource[F, HikariTransactor[F]] = {
    Resource
      .eval {
        DatabaseDriver
          .infer(databaseConfiguration.url)
          .toF[Throwable, F] {
            new IllegalArgumentException(s"Unable to infer database driver from ${databaseConfiguration.url}")
          }
      }
      .flatMap { databaseDriver =>
        for {
          connectEC <- ExecutionContexts.fixedThreadPool(4)

          transactor <- HikariTransactor.newHikariTransactor(
            databaseDriver.clazz.getName,
            databaseConfiguration.url,
            databaseConfiguration.user,
            databaseConfiguration.password,
            connectEC,
            ioBlocker
          )
        } yield transactor
      }
  }

}
