package com.ruchij.daos.user

import com.ruchij.daos.user.models.User

trait UserDao[F[_]] {

  def insert(user: User): F[Int]

  def findByUserId(userId: String): F[Option[User]]

  def findByUsername(username: String): F[Option[User]]

  def findByEmail(email: String): F[Option[User]]

}
