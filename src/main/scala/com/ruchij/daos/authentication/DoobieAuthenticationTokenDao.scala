package com.ruchij.daos.authentication

import cats.Applicative
import com.ruchij.daos.authentication.models.AuthenticationToken
import com.ruchij.daos.doobie.GetMappings._
import com.ruchij.daos.doobie.PutMappings._
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

object DoobieAuthenticationTokenDao extends AuthenticationTokenDao[ConnectionIO] {
  override def insert(authenticationToken: AuthenticationToken): ConnectionIO[Int] =
    sql"""
      INSERT INTO authentication_token (user_id, created_at, modified_at, secret, expires_at, renewals)
        VALUES (
          ${authenticationToken.userId},
          ${authenticationToken.createdAt},
          ${authenticationToken.modifiedAt},
          ${authenticationToken.secret},
          ${authenticationToken.expiresAt},
          ${authenticationToken.renewals}
        )
    """
      .update
      .run

  override def find(userId: String, secret: String): ConnectionIO[Option[AuthenticationToken]] =
    sql"""
      SELECT user_id, created_at, modified_at, secret, expires_at, renewals
        FROM authentication_token WHERE user_id = $userId AND secret = $secret
    """
      .query[AuthenticationToken]
      .option

  override def update(authenticationToken: AuthenticationToken): ConnectionIO[Int] =
    sql"""
      UPDATE authentication_token
        SET
          modified_at = ${authenticationToken.modifiedAt},
          secret = ${authenticationToken.secret},
          expires_at = ${authenticationToken.expiresAt},
          renewals = ${authenticationToken.renewals}
        WHERE user_id = ${authenticationToken.userId} AND created_at = ${authenticationToken.createdAt}
    """
      .update
      .run

  override def delete(userId: String, secret: String): ConnectionIO[Option[AuthenticationToken]] =
    find(userId, secret)
      .flatMap {
        _.fold[ConnectionIO[Option[AuthenticationToken]]](Applicative[ConnectionIO].pure(None)) {
          authenticationToken =>
            sql"DELETE FROM authentication_token WHERE user_id = $userId AND secret = $secret"
              .update
              .run
              .map {
                case 0 => None

                case _ => Some(authenticationToken)
              }
        }
      }
}
