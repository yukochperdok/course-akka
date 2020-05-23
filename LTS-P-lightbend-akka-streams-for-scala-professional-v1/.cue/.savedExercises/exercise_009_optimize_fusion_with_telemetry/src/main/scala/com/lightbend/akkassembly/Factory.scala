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
      .via(paintShop.paint.named("paint-stage")) //6millis
      .async
      .via(engineShop.installEngine.named("install-engine-stage")) //7millis
      .async
      .via(wheelShop.installWheels.named("install-wheels-stage")) //5millis
      .async
      .via(upgradeShop.installUpgrades.named("install-upgrades-stage")) //4millis
      .via(qa.inspect.named("inspect-stage"))
      .take(quantity)
      .runWith(Sink.seq)
  }

}
