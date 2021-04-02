package com.ruchij.daos.color.models

import org.joda.time.DateTime

case class Color(userId: String, createdAt: DateTime, modifiedAt: DateTime, colorValue: ColorValue)
