package com.ruchij.web.middleware

import cats.Applicative
import cats.data.Kleisli
import cats.effect.Sync
import cats.implicits._
import com.ruchij.types.RandomGenerator
import com.ruchij.web.headers.`X-Correlation-ID`
import org.http4s.{ContextRequest, HttpApp, Response}

import java.util.UUID

object CorrelationIdMiddleware {
  type CorrelationID = String
  type HttpAppWithContext[F[_], A] = Kleisli[F, ContextRequest[F, A], Response[F]]

  def apply[F[_]: Sync: RandomGenerator[*[_], UUID]](httpRoutes: HttpAppWithContext[F, CorrelationID]): HttpApp[F] =
    Kleisli { request =>
      request.headers
        .get(`X-Correlation-ID`)
        .fold(RandomGenerator[F, UUID].generate.map(uuid => `X-Correlation-ID`(uuid.toString))) {
          header => Applicative[F].pure(header)
        }
        .flatMap {
          case `X-Correlation-ID`(correlationId) =>
            httpRoutes
              .run(ContextRequest(correlationId, request))
              .map { response =>
                response.putHeaders(`X-Correlation-ID`(correlationId))
              }
        }
    }
}
