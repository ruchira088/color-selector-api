package com.ruchij.daos.authentication

import com.ruchij.daos.authentication.models.AuthenticationToken

trait AuthenticationTokenDao[F[_]] {

  def insert(authenticationToken: AuthenticationToken): F[Int]

  def find(userId: String, secret: String): F[Option[AuthenticationToken]]

  def update(authenticationToken: AuthenticationToken): F[Int]

  def delete(userId: String, secret: String): F[Option[AuthenticationToken]]

}
