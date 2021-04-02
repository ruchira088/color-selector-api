package com.ruchij.services.authentication

import com.ruchij.daos.authentication.models.AuthenticationToken
import com.ruchij.daos.user.models.User

trait AuthenticationService[F[_]] {

  def token(username: String, password: String): F[AuthenticationToken]

  def authenticate(bearerToken: String): F[User]

}
