package com.ruchij.web.routes

import cats.effect.Sync
import cats.implicits._
import com.ruchij.services.authentication.AuthenticationService
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID
import org.http4s.ContextRoutes
import org.http4s.dsl.Http4sDsl

object SessionRoutes {

  def apply[F[_]: Sync](
    authenticationService: AuthenticationService[F]
  )(implicit dsl: Http4sDsl[F]): ContextRoutes[CorrelationID, F] = {
    import dsl._

    ContextRoutes.of[CorrelationID, F] {

      case (request @ POST -> Root) as _ =>


    }

  }

}
