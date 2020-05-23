package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}

import scala.collection.immutable

case class WheelShop() {

  val wheels: Source[Wheel, NotUsed] = Source.repeat(Wheel())

  private val groupedWheels: Source[immutable.Seq[Wheel], NotUsed] = wheels.grouped(4)

  val installWheels: Flow[UnfinishedCar, UnfinishedCar, NotUsed] =
    Flow[UnfinishedCar]
      .zip(groupedWheels)
      .map{
        case (car, seqWheels) => car.installWheels(seqWheels)
      }


}
