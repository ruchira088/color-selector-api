package com.ruchij.daos.color

import cats.Applicative
import com.ruchij.daos.color.models.Color
import com.ruchij.daos.doobie.GetMappings._
import com.ruchij.daos.doobie.PutMappings._
import doobie.ConnectionIO
import doobie.implicits.toSqlInterpolator

import java.util.UUID

object DoobieColorDao extends ColorDao[ConnectionIO] {
  override def insert(color: Color): ConnectionIO[Int] =
    sql"""
      INSERT INTO color (user_id, created_at, modified_at, color_value)
        VALUES (${color.userId}, ${color.createdAt}, ${color.modifiedAt}, ${color.colorValue})
    """
      .update
      .run

  override def findByUserId(userId: UUID): ConnectionIO[Option[Color]] =
    sql"SELECT user_id, created_at, modified_at, color_value FROM color WHERE user_id = $userId"
      .query[Color]
      .option

  override def deleteByUserId(userId: UUID): ConnectionIO[Option[Color]] =
    findByUserId(userId)
      .flatMap {
        _.fold[ConnectionIO[Option[Color]]](Applicative[ConnectionIO].pure(None)) { color =>
          sql"DELETE FROM color WHERE user_id = $userId".update.run
            .map {
              case 0 => None

              case _ => Some(color)
            }
        }
      }
}
