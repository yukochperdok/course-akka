package com.lightbend.akkassembly

import akka.stream.Materializer
import akka.stream.scaladsl.Sink

import scala.concurrent.Future

case class Factory(
  bodyShop: BodyShop,
  paintShop: PaintShop,
  engineShop: EngineShop,
  wheelShop: WheelShop,
  qa: QualityAssurance,
  upgradeShop: UpgradeShop
)(implicit mat: Materializer) {

  def orderCars(quantity: Int): Future[Seq[Car]] = {
    bodyShop.cars
      .via(paintShop.paint)
      .via(engineShop.installEngine)
      .async
      .via(wheelShop.installWheels)
      .async
      .via(upgradeShop.installUpgrades)
      .via(qa.inspect)
      .take(quantity)
      .runWith(Sink.collection)
  }

}
