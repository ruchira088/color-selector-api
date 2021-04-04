package com.ruchij.daos.user.models

import org.joda.time.DateTime

import java.util.UUID

case class User(
  id: UUID,
  createdAt: DateTime,
  modifiedAt: DateTime,
  username: String,
  firstName: String,
  lastName: String,
  email: Email
)
