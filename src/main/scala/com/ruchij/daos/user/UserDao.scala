package com.ruchij.daos.user

import com.ruchij.daos.user.models.User

import java.util.UUID

trait UserDao[F[_]] {

  def insert(user: User): F[Int]

  def search(username: Option[String], offset: Int, pageSize: Int): F[List[User]]

  def findByUserId(userId: UUID): F[Option[User]]

  def findByUsername(username: String): F[Option[User]]

  def findByEmail(email: String): F[Option[User]]

}
