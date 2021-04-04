package com.ruchij.types

import cats.data.NonEmptyList

trait Parser[-A, +B] {
  def parse(input: A): Either[NonEmptyList[String], B]
}