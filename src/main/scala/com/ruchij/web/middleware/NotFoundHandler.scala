package com.ruchij.web.middleware

import cats.data.Kleisli
import cats.effect.Sync
import com.ruchij.exceptions.ResourceNotFoundException
import com.ruchij.web.middleware.CorrelationIdMiddleware.HttpAppWithContext
import org.http4s.{ContextRequest, ContextRoutes, Response}

object NotFoundHandler {

  def apply[F[_]: Sync, A](
    httpRoutes: ContextRoutes[A, F]
  ): HttpAppWithContext[F, A] =
    Kleisli[F, ContextRequest[F, A], Response[F]] { contextRequest =>
      httpRoutes.run(contextRequest).getOrElseF {
        Sync[F].raiseError {
          ResourceNotFoundException(s"Endpoint not found: ${contextRequest.req.method} ${contextRequest.req.uri}")
        }
      }
    }
}
