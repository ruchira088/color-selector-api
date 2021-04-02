package com.ruchij

import cats.{Applicative, ApplicativeError}

package object syntax {

  implicit class OptionWrapper[A](value: Option[A]) {
    def toF[E, F[_]: ApplicativeError[*[_], E]](onEmpty: => E): F[A] =
      value.fold[F[A]](ApplicativeError[F, E].raiseError(onEmpty))(result => Applicative[F].pure(result))

    def nonEmptyF[E, F[_]: ApplicativeError[*[_], E]](onNonEmpty: => E): F[Unit] =
      value.fold(Applicative[F].unit)(_ => ApplicativeError[F, E].raiseError(onNonEmpty))
  }

}
