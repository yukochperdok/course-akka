package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.ActorAttributes
import akka.stream.Supervision.{Decider, Resume, Stop}
import akka.stream.scaladsl.Flow

object QualityAssurance{
  case class CarFailedInspection(car: UnfinishedCar)
    extends IllegalStateException(s"Unappropriated car found: ${car}")
}

case class QualityAssurance() {

  private val decider: Decider = {
    case _: QualityAssurance.CarFailedInspection => Resume
    case _ => Stop
  }

  val inspect: Flow[UnfinishedCar, Car, NotUsed] =
    Flow[UnfinishedCar]
      .map{
        case unfinishCar if unfinishCar.isCompleted => Car(unfinishCar)
        case unfinishCar => throw QualityAssurance.CarFailedInspection(unfinishCar)
      }.withAttributes(ActorAttributes.supervisionStrategy(decider))
}
