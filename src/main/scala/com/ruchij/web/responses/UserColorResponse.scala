package com.ruchij.web.responses

import com.ruchij.daos.color.models.{Color, ColorValue}

case class UserColorResponse(colors: List[ColorValue])

object UserColorResponse {
  def from(colors: List[Color]): UserColorResponse =
    UserColorResponse {
      colors.map(_.colorValue)
    }
}
