package com.ruchij.services.authentication

import com.ruchij.daos.authentication.models.AuthenticationToken
import com.ruchij.daos.user.models.User

trait AuthenticationService[F[_]] {

  def login(username: String, password: String): F[AuthenticationToken]

  def authenticate(userId: String, secret: String): F[User]

  def logout(userId: String, secret: String): F[AuthenticationToken]

}
