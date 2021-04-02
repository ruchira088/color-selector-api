package com.ruchij.services.color

import com.ruchij.daos.color.models.{Color, ColorValue}
import com.ruchij.services.models.AuthenticatedRequestContext

trait ColorService[F[_]] {

  def insert(userId: String, colorValue: ColorValue)(implicit context: AuthenticatedRequestContext): F[Color]

  def findByUserId(userId: String)(implicit context: AuthenticatedRequestContext): F[List[Color]]

}
