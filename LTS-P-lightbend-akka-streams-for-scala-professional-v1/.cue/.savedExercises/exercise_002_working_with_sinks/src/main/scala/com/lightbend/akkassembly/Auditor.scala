package com.lightbend.akkassembly

import akka.Done
import akka.event.LoggingAdapter
import akka.stream.scaladsl.Sink

import scala.concurrent.Future

case class Auditor() {

  val count: Sink[Any, Future[Int]] =
    Sink.fold(0){case (count, _) => count + 1}


  def log(implicit logger: LoggingAdapter): Sink[Any, Future[Done]] =
    Sink.foreach(
      value => logger.debug(s"$value")
    )

}
