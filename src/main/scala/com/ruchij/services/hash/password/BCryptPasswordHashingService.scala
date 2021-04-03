package com.ruchij.services.hash.password

import cats.effect.{Blocker, ContextShift, Sync}
import cats.implicits._
import org.mindrot.jbcrypt.BCrypt

class BCryptPasswordHashingService[F[_]: Sync: ContextShift](cpuBlocker: Blocker) extends PasswordHashingService[F] {
  override def hash(password: String): F[String] =
    cpuBlocker.blockOn {
      for {
        salt <- Sync[F].delay(BCrypt.gensalt())
        hashedPassword <- Sync[F].delay(BCrypt.hashpw(password, salt))
      }
      yield hashedPassword
    }

  override def checkPassword(input: String, hashedValue: String): F[Boolean] =
    cpuBlocker.delay {
      BCrypt.checkpw(input, hashedValue)
    }
}
