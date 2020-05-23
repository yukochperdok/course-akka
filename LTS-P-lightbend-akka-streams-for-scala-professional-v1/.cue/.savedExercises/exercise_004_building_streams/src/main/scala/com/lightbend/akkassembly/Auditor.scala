package com.lightbend.akkassembly

import akka.{Done, NotUsed}
import akka.event.LoggingAdapter
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

case class Auditor()(implicit mat: Materializer) {

  val count: Sink[Any, Future[Int]] =
    Sink.fold(0){case (count, _) => count + 1}


  def log(implicit logger: LoggingAdapter): Sink[Any, Future[Done]] =
    Sink.foreach(
      value => logger.debug(s"$value")
    )

  def sample(sampleSize: FiniteDuration): Flow[Car, Car, NotUsed] =
    Flow[Car].takeWithin(sampleSize)

  def audit(cars: Source[Car, NotUsed], sampleSize: FiniteDuration): Future[Int] =
    cars.via(sample(sampleSize)).toMat(count)(Keep.right).run()

}
