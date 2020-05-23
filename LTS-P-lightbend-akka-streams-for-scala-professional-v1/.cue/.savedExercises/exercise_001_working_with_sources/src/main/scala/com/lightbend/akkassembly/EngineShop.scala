package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.scaladsl.Source

case class EngineShop(shipmentSize: Int) {
  val shipments: Source[Shipment, NotUsed] =
    Source.cycle {
      () =>
        Iterator.continually{
          Shipment(Seq.fill(shipmentSize)(Engine()).toList)
        }
    }

}
