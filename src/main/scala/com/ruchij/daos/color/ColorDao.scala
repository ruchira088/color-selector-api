package com.ruchij.daos.color

import com.ruchij.daos.color.models.Color

trait ColorDao[F[_]] {

  def insert(color: Color): F[Int]

  def findByUserId(userId: String): F[Option[Color]]

  def deleteByUserId(userId: String): F[Option[Color]]

}
