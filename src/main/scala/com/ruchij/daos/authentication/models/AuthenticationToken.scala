package com.ruchij.daos.authentication.models

import org.joda.time.DateTime

import java.util.UUID

case class AuthenticationToken(
  userId: UUID,
  createdAt: DateTime,
  modifiedAt: DateTime,
  secret: String,
  expiresAt: DateTime,
  renewals: Long
)
