package com.ruchij.web.routes

import cats.effect.Sync
import cats.implicits._
import com.ruchij.circe.Decoders._
import com.ruchij.circe.Encoders._
import com.ruchij.services.authentication.AuthenticationService
import com.ruchij.services.authorization.AuthorizationService
import com.ruchij.services.color.ColorService
import com.ruchij.services.models.AuthenticatedContext
import com.ruchij.services.user.UserService
import com.ruchij.web.middleware.AuthenticationMiddleware
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID
import com.ruchij.web.requests.{AuthorizationRequest, CreateUserRequest, SelectColorRequest}
import com.ruchij.web.responses.UserColorResponse
import io.circe.generic.auto._
import org.http4s.ContextRoutes
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.Http4sDsl

object UserRoutes {

  def apply[F[_]: Sync](
    userService: UserService[F],
    colorService: ColorService[F],
    authorizationService: AuthorizationService[F],
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
        ContextRoutes.of[AuthenticatedContext, F] {

          case (request @ POST -> Root / UUIDVar(userId) / "color") as authenticatedRequestContext =>
            for {
              colorValue <- request.as[SelectColorRequest].map(_.color)

              color <- colorService.insert(userId, colorValue)(authenticatedRequestContext)

              response <- Created(color)
            }
            yield response

          case GET -> Root / UUIDVar(userId) / "color" as authenticatedRequestContext =>
            for {
              colors <- colorService.findByUserId(userId)(authenticatedRequestContext)

              response <- Ok(UserColorResponse.from(colors))
            }
            yield response

          case (request @ POST -> Root / UUIDVar(userId) / "permission") as authenticatedRequestContext =>
            for {
              AuthorizationRequest(grantee, permissionType) <- request.as[AuthorizationRequest]

              permission <-
                authorizationService.grantPermission(grantee, userId, permissionType)(authenticatedRequestContext)

              response <- Created(permission)
            }
            yield response
        }
      }
  }

}
