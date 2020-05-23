package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.scaladsl.Source

case class PaintShop(colorSet: Set[Color]) {
  val colors: Source[Color, NotUsed] = {
    Source.cycle(
      () => colorSet.toIterator
    )
  }

}
