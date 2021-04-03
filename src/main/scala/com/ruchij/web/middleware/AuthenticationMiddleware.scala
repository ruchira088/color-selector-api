package com.ruchij.web.middleware

import cats.{ApplicativeError, MonadError}
import cats.data.{Kleisli, OptionT}
import com.ruchij.exceptions.ResourceNotFoundException
import com.ruchij.services.authentication.AuthenticationService
import com.ruchij.services.models.AuthenticatedContext
import com.ruchij.syntax._
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID
import org.http4s.Credentials.Token
import org.http4s.headers.Authorization
import org.http4s.{AuthScheme, ContextRequest, Request, Response}

import scala.util.matching.Regex

object AuthenticationMiddleware {
  val BearerToken: Regex = "(\\S+):(\\S+)".r

  def apply[F[_]: MonadError[*[_], Throwable]](
    authenticationService: AuthenticationService[F]
  ): Kleisli[OptionT[F, *], ContextRequest[F, AuthenticatedContext], Response[F]] => Kleisli[OptionT[F, *], ContextRequest[F, CorrelationID], Response[F]] =
    authenticatedHttpRoutes =>
      Kleisli { contextRequest =>
        for {
          (userId, secret) <- OptionT.liftF(bearerToken(contextRequest.req))

          user <- OptionT.liftF(authenticationService.authenticate(userId, secret))

          response <-
            authenticatedHttpRoutes.run {
              ContextRequest(AuthenticatedContext(user, contextRequest.context), contextRequest.req)
            }
        }
        yield response
    }

  def bearerToken[F[_]: ApplicativeError[*[_], Throwable]](request: Request[_]): F[(String, String)] =
    findBearerToken(request)
      .toF[Throwable, F](ResourceNotFoundException("Missing or invalid Authorization header with Bearer token"))

  private def findBearerToken(request: Request[_]): Option[(String, String)] =
    request.headers
      .get(Authorization)
      .collect {
        case Authorization(Token(AuthScheme.Bearer, bearerToken)) => bearerToken
      }
      .collect {
        case BearerToken(userId, secret) => (userId, secret)
      }

}
