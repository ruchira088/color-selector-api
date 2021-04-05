package com.ruchij.web

import cats.effect.Sync
import com.ruchij.services.authentication.AuthenticationService
import com.ruchij.services.authorization.AuthorizationService
import com.ruchij.services.color.ColorService
import com.ruchij.services.health.HealthService
import com.ruchij.services.user.UserService
import com.ruchij.types.RandomGenerator
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID
import com.ruchij.web.middleware.{CorrelationIdMiddleware, ExceptionHandler, NotFoundHandler}
import com.ruchij.web.routes.{HealthRoutes, SearchRoutes, SessionRoutes, UserRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.ContextRouter
import org.http4s.{ContextRoutes, HttpApp}

import java.util.UUID

object Routes {
  def apply[F[_]: Sync: RandomGenerator[*[_], UUID]](
    userService: UserService[F],
    colorService: ColorService[F],
    authorizationService: AuthorizationService[F],
    authenticationService: AuthenticationService[F],
    healthService: HealthService[F]
  ): HttpApp[F] = {
    implicit val dsl: Http4sDsl[F] = new Http4sDsl[F] {}

    val routes: ContextRoutes[CorrelationID, F] =
      ContextRouter(
        "/service" -> HealthRoutes(healthService),
        "/user" -> UserRoutes(userService, colorService, authorizationService, authenticationService),
        "/search" -> SearchRoutes(userService, authenticationService),
        "/session" -> SessionRoutes(authenticationService)
      )

    CorrelationIdMiddleware {
      ExceptionHandler {
        NotFoundHandler(routes)
      }
    }
  }
}
