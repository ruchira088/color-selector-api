package com.ruchij.web.routes

import cats.effect.Sync
import cats.implicits._
import com.ruchij.circe.Encoders.dateTimeEncoder
import com.ruchij.services.authentication.AuthenticationService
import com.ruchij.services.models.AuthenticatedContext
import com.ruchij.web.middleware.AuthenticationMiddleware
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID
import com.ruchij.web.requests.AuthenticationRequest
import io.circe.generic.auto._
import org.http4s.ContextRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object SessionRoutes {

  def apply[F[_]: Sync](
    authenticationService: AuthenticationService[F]
  )(implicit dsl: Http4sDsl[F]): ContextRoutes[CorrelationID, F] = {
    import dsl._

    ContextRoutes.of[CorrelationID, F] {
      case (request @ POST -> Root) as _ =>
        for {
          AuthenticationRequest(username, password) <- request.as[AuthenticationRequest]

          authenticationToken <- authenticationService.login(username, password)

          response <- Created(authenticationToken)
        }
        yield response
    } <+>
      AuthenticationMiddleware(authenticationService).apply {
        ContextRoutes.of[AuthenticatedContext, F] {
          case GET -> Root / "user" as AuthenticatedContext(user, _) => Ok(user)

          case (request @ DELETE -> Root) as _ =>
            for {
              (userId, secret) <- AuthenticationMiddleware.bearerToken(request)

              authenticationToken <- authenticationService.logout(userId, secret)

              response <- Ok(authenticationToken)
            }
            yield response

        }
      }
  }

}
