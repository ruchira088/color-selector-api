package com.ruchij.services.hash.password

trait PasswordHashingService[F[_]] {
  def hash(password: String): F[String]

  def checkPassword(input: String, hashedValue: String): F[Boolean]
}
