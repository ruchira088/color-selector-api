package com.ruchij.daos.color.models

import org.joda.time.DateTime

import java.util.UUID

case class Color(userId: UUID, createdAt: DateTime, modifiedAt: DateTime, colorValue: ColorValue)
