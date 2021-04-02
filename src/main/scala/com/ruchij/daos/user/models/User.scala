package com.ruchij.daos.user.models

import org.joda.time.DateTime

case class User(
  id: String,
  createdAt: DateTime,
  modifiedAt: DateTime,
  username: String,
  firstName: String,
  lastName: String,
  email: String
)