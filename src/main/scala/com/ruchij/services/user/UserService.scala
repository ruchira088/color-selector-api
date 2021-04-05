package com.ruchij.services.user

import com.ruchij.daos.user.models.{Email, User}

trait UserService[F[_]] {

  def create(username: String, password: String, firstName: String, lastName: String, email: Email): F[User]

  def search(username: Option[String], offset: Int, pageSize: Int): F[List[User]]

  def usernameExists(username: String): F[Boolean]
}
