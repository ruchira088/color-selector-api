package com.ruchij.types

import cats.data.{NonEmptyList, ValidatedNel}
import cats.effect.Sync
import cats.{Applicative, ApplicativeError, Semigroup, ~>}
import doobie.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor

object FunctionKTypes {

  implicit def eitherToF[L, F[_]: ApplicativeError[*[_], L]]: Either[L, *] ~> F =
    new ~>[Either[L, *], F] {
      override def apply[A](either: Either[L, A]): F[A] =
        either.fold(ApplicativeError[F, L].raiseError, Applicative[F].pure)
    }

  def connectionIoToF[F[_]: Sync](transactor: Transactor[F]): ConnectionIO ~> F =
    new ~>[ConnectionIO, F] {
      override def apply[A](connectionIO: ConnectionIO[A]): F[A] =
        Sync[F].defer {
          connectionIO.transact(transactor)
        }
    }

  implicit def validatedNelToF[L: Semigroup, F[_]: ApplicativeError[*[_], L]]: ValidatedNel[L, *] ~> F =
    new ~>[ValidatedNel[L, *], F] {
      override def apply[A](validatedNel: ValidatedNel[L, A]): F[A] =
        validatedNel.fold[F[A]]({
          case NonEmptyList(head, tail) =>
            ApplicativeError[F, L].raiseError {
              tail.foldLeft(head) { case (acc, value) => Semigroup[L].combine(acc, value) }
            }
          },
          result => Applicative[F].pure(result)
        )
    }

  def identityFunctionK[F[_]]: F ~> F = new ~>[F, F] {
    override def apply[A](fa: F[A]): F[A] = fa
  }
}
