package com.ruchij.daos.color

import com.ruchij.daos.color.models.Color

import java.util.UUID

trait ColorDao[F[_]] {

  def insert(color: Color): F[Int]

  def findByUserId(userId: UUID): F[Option[Color]]

  def deleteByUserId(userId: UUID): F[Option[Color]]

}
