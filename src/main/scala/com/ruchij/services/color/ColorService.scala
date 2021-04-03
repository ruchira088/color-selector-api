package com.ruchij.services.color

import com.ruchij.daos.color.models.{Color, ColorValue}
import com.ruchij.services.models.AuthenticatedContext

import java.util.UUID

trait ColorService[F[_]] {

  def insert(userId: UUID, colorValue: ColorValue)(implicit context: AuthenticatedContext): F[Color]

  def findByUserId(userId: UUID)(implicit context: AuthenticatedContext): F[List[Color]]

}
