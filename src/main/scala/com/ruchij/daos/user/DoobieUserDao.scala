package com.ruchij.daos.user

import com.ruchij.daos.user.models.{Email, User}
import doobie.ConnectionIO
import doobie.util.fragments
import doobie.implicits.toSqlInterpolator
import com.ruchij.daos.doobie.GetMappings._
import com.ruchij.daos.doobie.PutMappings._

import java.util.UUID

object DoobieUserDao extends UserDao[ConnectionIO] {
  val SelectQuery =
    fr"SELECT id, created_at, modified_at, username, first_name, last_name, email FROM user_info"

  override def insert(user: User): ConnectionIO[Int] =
    sql"""
      INSERT INTO user_info (id, created_at, modified_at, username, first_name, last_name, email)
        VALUES (
          ${user.id},
          ${user.createdAt},
          ${user.modifiedAt},
          ${user.username},
          ${user.firstName},
          ${user.lastName},
          ${user.email.value}
        )
    """
      .update
      .run

  override def findByUserId(userId: UUID): ConnectionIO[Option[User]] =
    (SelectQuery ++ fr"WHERE id = $userId").query[User].option

  override def findByUsername(username: String): ConnectionIO[Option[User]] =
    (SelectQuery ++ fr"WHERE username = $username").query[User].option

  override def findByEmail(email: Email): ConnectionIO[Option[User]] =
    (SelectQuery ++ fr"WHERE email = ${email.value}").query[User].option

  override def search(maybeUsername: Option[String], offset: Int, pageSize: Int): ConnectionIO[List[User]] =
    (SelectQuery ++
      fragments.whereAndOpt(maybeUsername.map(username => fr"username LIKE ${"%" + username + "%"}")) ++
      fr" LIMIT $pageSize OFFSET ${offset * pageSize}")
      .query[User]
      .to[List]
}
