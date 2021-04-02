package com.ruchij.web.routes

import cats.effect.Sync
import cats.implicits._
import com.ruchij.services.authentication.AuthenticationService
import com.ruchij.services.color.ColorService
import com.ruchij.services.models.AuthenticatedRequestContext
import com.ruchij.services.user.UserService
import com.ruchij.web.middleware.AuthenticationMiddleware
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID
import com.ruchij.web.requests.{CreateUserRequest, SelectColorRequest}
import com.ruchij.web.responses.UserColorResponse
import io.circe.generic.auto._
import org.http4s.ContextRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object UserRoutes {

  def apply[F[_]: Sync](
    userService: UserService[F],
    colorService: ColorService[F],
    authenticationService: AuthenticationService[F]
  )(implicit dsl: Http4sDsl[F]): ContextRoutes[CorrelationID, F] = {
    import dsl._

    ContextRoutes.of[CorrelationID, F] {
      case (request @ POST -> Root) as _ =>
        for {
          CreateUserRequest(username, password, firstName, lastName, email) <- request.as[CreateUserRequest]

          user <- userService.create(username, password, firstName, lastName, email)

          response <- Created(user)
        } yield response
    } <+>
      AuthenticationMiddleware(authenticationService).apply {
        ContextRoutes.of[AuthenticatedRequestContext, F] {
          case GET -> Root as AuthenticatedRequestContext(user, _) => Ok(user)

          case (request @ POST -> Root / userId / "color") as authenticatedRequestContext =>
            for {
              colorValue <- request.as[SelectColorRequest].map(_.color)

              color <- colorService.insert(userId, colorValue)(authenticatedRequestContext)

              response <- Created(color)
            }
            yield response

          case GET -> Root / userId / "color" as authenticatedRequestContext =>
            for {
              colors <- colorService.findByUserId(userId)(authenticatedRequestContext)

              response <- Ok(UserColorResponse(colors))
            }
            yield response
        }
      }
  }

}
