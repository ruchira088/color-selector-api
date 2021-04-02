package com.ruchij.web.middleware

import cats.MonadError
import cats.data.{Kleisli, OptionT}
import com.ruchij.exceptions.ResourceNotFoundException
import com.ruchij.services.authentication.AuthenticationService
import com.ruchij.services.models.AuthenticatedRequestContext
import com.ruchij.syntax._
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID
import org.http4s.Credentials.Token
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, ContextRequest, Request, Response}

object AuthenticationMiddleware {

  def apply[F[_]: MonadError[*[_], Throwable]](
    authenticationService: AuthenticationService[F]
  ): Kleisli[OptionT[F, *], ContextRequest[F, AuthenticatedRequestContext], Response[F]] => Kleisli[OptionT[F, *], ContextRequest[F, CorrelationID], Response[F]] =
    authenticatedHttpRoutes =>
      Kleisli { contextRequest =>
        for {
          bearerToken <-
            OptionT.liftF(findBearerToken(contextRequest.req)
              .toF[Throwable, F](ResourceNotFoundException("Missing Authorization header with Bearer token")))

          user <- OptionT.liftF(authenticationService.authenticate(bearerToken))

          response <-
            authenticatedHttpRoutes.run {
              ContextRequest(AuthenticatedRequestContext(user, contextRequest.context), contextRequest.req)
            }
        }
        yield response
    }

  def findBearerToken(request: Request[_]): Option[String] =
    request.headers
      .get(Authorization)
      .collect {
        case Authorization(Token(AuthScheme.Bearer, bearerToken)) => bearerToken
      }

}
