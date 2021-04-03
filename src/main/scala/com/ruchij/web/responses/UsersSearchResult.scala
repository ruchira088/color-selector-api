package com.ruchij.web.responses

import com.ruchij.daos.user.models.User
import com.ruchij.web.responses.UsersSearchResult.UserInfo

import java.util.UUID

case class UsersSearchResult(results: Seq[UserInfo], offset: Int, pageSize: Int, username: Option[String])

object UsersSearchResult {
  case class UserInfo(userId: UUID, username: String, firstName: String, lastName: String)

  def apply(users: List[User], offset: Int, pageSize: Int, maybeUsername: Option[String]): UsersSearchResult =
    UsersSearchResult(
      users.map(user => UserInfo(user.id, user.username, user.firstName, user.lastName)),
      offset,
      pageSize,
      maybeUsername
    )
}