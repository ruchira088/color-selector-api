package com.ruchij.exceptions

import cats.data.NonEmptyList
import cats.kernel.Semigroup

case class AggregatedException[+A <: Throwable](exceptions: NonEmptyList[A]) extends Exception

object AggregatedException {
  implicit val throwableSemigroup: Semigroup[Throwable] = {
    case (AggregatedException(exceptionsOne), AggregatedException(exceptionsTwo)) =>
      AggregatedException(exceptionsOne ::: exceptionsTwo)

    case (exception, AggregatedException(exceptions)) => AggregatedException(exception :: exceptions)

    case (AggregatedException(exceptions), exception) => AggregatedException(exceptions ++ List(exception))

    case (exceptionOne, exceptionTwo) => AggregatedException(NonEmptyList.of(exceptionOne, exceptionTwo))
  }
}
