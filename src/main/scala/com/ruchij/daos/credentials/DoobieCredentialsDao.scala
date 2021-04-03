package com.ruchij.daos.credentials

import com.ruchij.daos.doobie.GetMappings._
import com.ruchij.daos.doobie.PutMappings._
import com.ruchij.daos.credentials.models.Credentials
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

import java.util.UUID

object DoobieCredentialsDao extends CredentialsDao[ConnectionIO] {
  override def insert(credentials: Credentials): ConnectionIO[Int] =
    sql"""
      INSERT INTO credentials (user_id, created_at, modified_at, salted_hashed_password)
        VALUES (
          ${credentials.userId},
          ${credentials.createdAt},
          ${credentials.modifiedAt},
          ${credentials.saltedHashedPassword}
        )
    """
      .update
      .run

  override def findByUserId(userId: UUID): ConnectionIO[Option[Credentials]] =
    sql"""
      SELECT user_id, created_at, modified_at, salted_hashed_password FROM credentials
        WHERE user_id = $userId
    """
      .query[Credentials]
      .option
}
