package com.ruchij.services.user

import com.ruchij.daos.user.models.User

trait UserService[F[_]] {

  def create(username: String, password: String, firstName: String, lastName: String, email: String): F[User]

  def usernameAvailable(username: String): F[Boolean]

}
