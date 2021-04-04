package com.ruchij.web.responses

import com.ruchij.daos.user.models.{Email, User}

import java.util.UUID

case class UserResponse(id: UUID, username: String, firstName: String, lastName: String, email: Email)

object UserResponse {
  def apply(user: User): UserResponse =
    UserResponse(user.id, user.username, user.firstName, user.lastName, user.email)
}
