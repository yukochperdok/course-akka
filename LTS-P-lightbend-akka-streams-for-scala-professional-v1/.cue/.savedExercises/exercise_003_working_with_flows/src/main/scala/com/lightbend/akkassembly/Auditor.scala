package com.lightbend.akkassembly

import akka.{Done, NotUsed}
import akka.event.LoggingAdapter
import akka.stream.scaladsl.{Flow, Sink}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

case class Auditor() {

  val count: Sink[Any, Future[Int]] =
    Sink.fold(0){case (count, _) => count + 1}


  def log(implicit logger: LoggingAdapter): Sink[Any, Future[Done]] =
    Sink.foreach(
      value => logger.debug(s"$value")
    )

  def sample(sampleSize: FiniteDuration): Flow[Car, Car, NotUsed] =
    Flow[Car].takeWithin(sampleSize)

}
