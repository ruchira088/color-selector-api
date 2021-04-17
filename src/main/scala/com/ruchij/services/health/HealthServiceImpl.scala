package com.ruchij.services.health

import cats.effect.{Clock, Concurrent, Fiber, Timer}
import cats.implicits._
import cats.~>
import com.ruchij.config.BuildInformation
import com.ruchij.daos.health.HealthCheckDao
import com.ruchij.services.health.models.{ServiceInformation, ServiceStatus, HealthStatus}
import org.joda.time.DateTime

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class HealthServiceImpl[F[_]: Timer: Concurrent, G[_]](healthCheckDao: HealthCheckDao[G], buildInformation: BuildInformation)(
  implicit transaction: G ~> F
) extends HealthService[F] {

  override val serviceInformation: F[ServiceInformation] =
    Clock[F]
      .realTime(TimeUnit.MILLISECONDS)
      .flatMap(timestamp => ServiceInformation.create(new DateTime(timestamp), buildInformation))

  override val serviceStatus: F[ServiceStatus] =
    for {
      databaseStatusFiber <-
        run {
          transaction(healthCheckDao.ok)
            .map(success => if (success) HealthStatus.OK else HealthStatus.Failure)
        }

      databaseStatus <- databaseStatusFiber.join
    }
    yield ServiceStatus(databaseStatus)


  def run(block: => F[HealthStatus]): F[Fiber[F, HealthStatus]] =
    Concurrent[F].start {
      Concurrent[F]
        .race(Timer[F].sleep(10 seconds).as(HealthStatus.Failure), block)
        .map(_.merge)
    }

}
