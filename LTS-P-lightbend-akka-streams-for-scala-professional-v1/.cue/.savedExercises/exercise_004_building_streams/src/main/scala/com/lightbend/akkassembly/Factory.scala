package com.lightbend.akkassembly

import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink}

import scala.concurrent.Future

case class Factory(
  bodyShop: BodyShop,
  paintShop: PaintShop,
  engineShop: EngineShop,
  wheelShop: WheelShop,
  qa: QualityAssurance
)(implicit mat: Materializer) {

  def orderCars(quantity: Int): Future[Seq[Car]] = {
    bodyShop.cars
      .via(paintShop.paint)
      .via(engineShop.installEngine)
      .via(wheelShop.installWheels)
      .via(qa.inspect)
      .take(quantity)
      .runWith(Sink.collection)
  }

}
