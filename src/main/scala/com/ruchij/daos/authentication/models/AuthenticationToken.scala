package com.ruchij.daos.authentication.models

import org.joda.time.DateTime

case class AuthenticationToken(
  userId: String,
  createdAt: DateTime,
  modifiedAt: DateTime,
  secret: String,
  expiresAt: DateTime,
  renewals: Long
)
