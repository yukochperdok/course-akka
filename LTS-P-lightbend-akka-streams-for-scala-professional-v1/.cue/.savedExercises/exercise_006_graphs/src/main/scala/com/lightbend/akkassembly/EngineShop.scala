package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}

case class EngineShop(shipmentSize: Int) {

  val shipments: Source[Shipment, NotUsed] =
    Source.cycle {
      () =>
        Iterator.continually{
          Shipment(Seq.fill(shipmentSize)(Engine()).toList)
        }
    }

  val engines: Source[Engine, NotUsed] =
    shipments.via(
      Flow[Shipment]
        .flatMapConcat{
          shipment => Source.fromIterator(() => shipment.engines.toIterator)
        }
    )
    // OR --> shipments.flatMapConcat(shipment => Source.fromIterator(() => shipment.engines.toIterator))

  val installEngine: Flow[UnfinishedCar, UnfinishedCar, NotUsed] =
    Flow[UnfinishedCar]
      .zip(engines)
      .map{
        case (car, engine) => car.installEngine(engine)
      }
}
