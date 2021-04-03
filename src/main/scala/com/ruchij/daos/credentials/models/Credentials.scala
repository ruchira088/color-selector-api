package com.ruchij.daos.credentials.models

import org.joda.time.DateTime

import java.util.UUID

case class Credentials(userId: UUID, createdAt: DateTime, modifiedAt: DateTime, saltedHashedPassword: String)