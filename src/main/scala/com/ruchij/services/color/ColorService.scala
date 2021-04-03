package com.ruchij.services.color

import com.ruchij.daos.color.models.{Color, ColorValue}
import com.ruchij.services.models.AuthenticatedContext

trait ColorService[F[_]] {

  def insert(userId: String, colorValue: ColorValue)(implicit context: AuthenticatedContext): F[Color]

  def findByUserId(userId: String)(implicit context: AuthenticatedContext): F[List[Color]]

}
