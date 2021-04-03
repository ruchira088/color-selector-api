package com.ruchij.web.responses

import com.ruchij.daos.authentication.models.AuthenticationToken
import org.joda.time.DateTime

import java.util.UUID

case class AuthenticationTokenResponse(userId: UUID, secret: String, createdAt: DateTime)

object AuthenticationTokenResponse {
  def apply(authenticationToken: AuthenticationToken): AuthenticationTokenResponse =
    AuthenticationTokenResponse(authenticationToken.userId, authenticationToken.secret, authenticationToken.createdAt)
}
