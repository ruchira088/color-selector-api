package com.ruchij.web.responses

import com.ruchij.daos.authentication.models.AuthenticationToken
import org.joda.time.DateTime

case class AuthenticationTokenResponse(userId: String, secret: String, createdAt: DateTime)

object AuthenticationTokenResponse {
  def apply(authenticationToken: AuthenticationToken): AuthenticationTokenResponse =
    AuthenticationTokenResponse(authenticationToken.userId, authenticationToken.secret, authenticationToken.createdAt)
}
