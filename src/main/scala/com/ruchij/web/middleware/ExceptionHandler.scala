package com.ruchij.web.middleware

import cats.Show
import cats.arrow.FunctionK
import cats.data.{Kleisli, NonEmptyList}
import cats.effect.Sync
import cats.implicits._
import com.ruchij.exceptions.{AggregatedException, AuthenticationException, AuthorizationException, ResourceConflictException, ResourceNotFoundException, ValidationException}
import com.ruchij.types.FunctionKTypes
import com.ruchij.web.middleware.CorrelationIdMiddleware.HttpAppWithContext
import com.ruchij.web.responses.ErrorResponse
import io.circe.{CursorOp, DecodingFailure}
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.dsl.impl.EntityResponseGenerator
import org.http4s.{ContextRequest, Response, Status}

object ExceptionHandler {
  def apply[F[_]: Sync, A](httpApp: HttpAppWithContext[F, A]): HttpAppWithContext[F, A] =
    Kleisli[F, ContextRequest[F, A], Response[F]] { contextRequest =>
      Sync[F].handleErrorWith(httpApp.run(contextRequest)) { throwable =>
        entityResponseGenerator[F](throwable)(throwableResponseBody(throwable))
          .map(errorResponseMapper(throwable))
      }
    }

  val throwableStatusMapper: Throwable => Status = {
    case _: ResourceNotFoundException => Status.NotFound

    case _: AuthenticationException => Status.Unauthorized

    case _: AuthorizationException => Status.Forbidden

    case _: ResourceConflictException => Status.Conflict

    case _: ValidationException => Status.BadRequest

    case AggregatedException(exceptions) => throwableStatusMapper(exceptions.head)

    case _ => Status.InternalServerError
  }

  val throwableResponseBody: Throwable => ErrorResponse = {
    case decodingFailure: DecodingFailure =>
        ErrorResponse {
          NonEmptyList.of {
            CursorOp.opsToPath(decodingFailure.history) + " " + decodingFailure.message
          }
        }

    case throwable =>
      Option(throwable.getCause).fold(ErrorResponse(NonEmptyList.of(throwable.getMessage)))(throwableResponseBody)
  }

  def errorResponseMapper[F[_]](throwable: Throwable)(response: Response[F]): Response[F] =
    throwable match {
      case _ => response
    }

  def entityResponseGenerator[F[_]](throwable: Throwable): EntityResponseGenerator[F, F] =
    new EntityResponseGenerator[F, F] {
      override def status: Status = throwableStatusMapper(throwable)

      override def liftG: FunctionK[F, F] = FunctionKTypes.identityFunctionK[F]
    }
}
