package com.lightbend.akkassembly

import akka.NotUsed
import akka.stream.scaladsl.Flow

case class QualityAssurance() {

  val inspect: Flow[UnfinishedCar, Car, NotUsed] =
    Flow[UnfinishedCar]
      .filter(_.isCompleted)
      .map(unfinishCar => Car(unfinishCar))

}
