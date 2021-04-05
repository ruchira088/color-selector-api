package com.ruchij.web.routes

import cats.effect.Sync
import cats.implicits._
import com.ruchij.exceptions.AggregatedException.throwableSemigroup
import com.ruchij.services.authentication.AuthenticationService
import com.ruchij.services.models.AuthenticatedContext
import com.ruchij.services.user.UserService
import com.ruchij.types.FunctionKTypes
import com.ruchij.web.middleware.AuthenticationMiddleware
import com.ruchij.web.middleware.CorrelationIdMiddleware.CorrelationID
import com.ruchij.web.models.Paging
import com.ruchij.web.queryparams.{OptionalUsernameQueryParamMatcher, PagingQueryParamMatcher}
import com.ruchij.web.responses.{UsernameExistsResponse, UsersSearchResult}
import io.circe.generic.auto._
import org.http4s.ContextRoutes
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.Http4sDsl

object SearchRoutes {

  def apply[F[_]: Sync](userService: UserService[F], authenticationService: AuthenticationService[F])(
    implicit dsl: Http4sDsl[F]
  ): ContextRoutes[CorrelationID, F] = {
    import dsl._

    ContextRoutes.of[CorrelationID, F] {
      case GET -> Root / "username" / username as _ =>
        for {
          exists <- userService.usernameExists(username)

          response <- Ok(UsernameExistsResponse(username, exists))
        }
        yield response

    } <+>
      AuthenticationMiddleware(authenticationService).apply {
        ContextRoutes.of[AuthenticatedContext, F] {
          case GET -> Root / "user" :? OptionalUsernameQueryParamMatcher(maybeUsername) +& PagingQueryParamMatcher(paging) as AuthenticatedContext(user, _) =>
            for {
              Paging(offset, pageSize) <- FunctionKTypes.validatedNelToF[Throwable, F].apply(paging)

              users <- userService.search(maybeUsername, offset, pageSize)

              response <- Ok(UsersSearchResult(users, offset, pageSize, maybeUsername))
            }
            yield response
        }
      }
  }

}
