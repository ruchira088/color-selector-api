package com.ruchij.web.routes

import cats.effect.Sync
import cats.implicits.toFlatMapOps
import com.ruchij.circe.Encoders.{dateTimeEncoder, enumEncoder}
import com.ruchij.services.health.HealthService
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID
import io.circe.generic.auto.exportEncoder
import org.http4s.ContextRoutes
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

object HealthRoutes {

  def apply[F[_]: Sync](healthService: HealthService[F])(implicit dsl: Http4sDsl[F]): ContextRoutes[CorrelationID, F] = {
    import dsl._

    ContextRoutes.of[CorrelationID, F] {
      case GET -> Root / "info" as _ =>
        healthService.serviceInformation
          .flatMap(serviceInformation => Ok(serviceInformation))

      case GET -> Root / "health-check" as _ =>
        healthService.serviceStatus
          .flatMap {
            serviceStatus =>
              if (serviceStatus.isHealthy) Ok(serviceStatus) else InternalServerError(serviceStatus)
          }
    }
  }

}
