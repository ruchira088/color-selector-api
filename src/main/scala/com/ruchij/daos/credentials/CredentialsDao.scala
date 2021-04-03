package com.ruchij.daos.credentials

import com.ruchij.daos.credentials.models.Credentials

import java.util.UUID

trait CredentialsDao[F[_]] {

  def insert(credentials: Credentials): F[Int]

  def findByUserId(userId: UUID): F[Option[Credentials]]

}
