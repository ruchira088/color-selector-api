package com.ruchij.types

import cats.effect.{IO, Sync}
import cats.{Applicative, Monad}
import cats.implicits._

import java.util.UUID

trait RandomGenerator[F[_], +A] {
  def generate[B >: A]: F[B]
}

object RandomGenerator {
  def apply[F[_], A](implicit randomGenerator: RandomGenerator[F, A]): RandomGenerator[F, A] = randomGenerator

  def apply[F[_]: Sync, A](thunk: => A): RandomGenerator[F, A] =
    new RandomGenerator[F, A] {
      override def generate[B >: A]: F[B] = Sync[F].delay(thunk)
    }

  implicit val randomUuidGenerator: RandomGenerator[IO, UUID] = RandomGenerator[IO, UUID](UUID.randomUUID())

  implicit def randomGeneratorMonad[F[_]: Monad]: Monad[RandomGenerator[F, *]] =
    new Monad[RandomGenerator[F, *]] {
      override def pure[A](value: A): RandomGenerator[F, A] =
        new RandomGenerator[F, A] {
          override def generate[B >: A]: F[B] = Applicative[F].pure[B](value)
        }

      override def flatMap[A, B](randomGenerator: RandomGenerator[F, A])(f: A => RandomGenerator[F, B]): RandomGenerator[F, B] =
        new RandomGenerator[F, B] {
          override def generate[C >: B]: F[C] =
            randomGenerator.generate.flatMap(value => f(value).generate)
        }

      override def tailRecM[A, B](value: A)(f: A => RandomGenerator[F, Either[A, B]]): RandomGenerator[F, B] =
        new RandomGenerator[F, B] {
          override def generate[C >: B]: F[C] =
            f(value).generate.flatMap {
              case Left(left) => tailRecM[A, B](left)(f).generate

              case Right(right) => Applicative[F].pure(right)
            }
        }
    }
}
