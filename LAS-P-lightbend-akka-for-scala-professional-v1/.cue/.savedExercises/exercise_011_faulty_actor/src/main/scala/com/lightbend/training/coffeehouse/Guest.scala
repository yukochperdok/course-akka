package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Timers}

import scala.concurrent.duration.FiniteDuration

object Guest {
  case object CoffeeFinished
  case object CaffeineException extends IllegalArgumentException

  def props(
    waiter: ActorRef,
    favouriteCoffee: Coffee,
    finishCoffeeDuration: FiniteDuration,
    caffeineLimit: Int): Props =
    Props(new Guest(waiter, favouriteCoffee, finishCoffeeDuration, caffeineLimit))
}

class Guest(
  waiter: ActorRef,
  favouriteCoffee: Coffee,
  finishCoffeeDuration: FiniteDuration,
  caffeineLimit: Int)
  extends Actor
    with ActorLogging
    with Timers {

  import Guest._

  override def postStop(): Unit = {
    log.info("Goodbye")
    super.postStop()
  }

  private var coffeeCounter = 0

  orderCoffee

  override def receive: Receive = {
    case Waiter.CoffeeServed(coffee) =>
      coffeeCounter += 1
      log.info(s"Enjoying my $coffeeCounter yummy $coffee")
      timers.startSingleTimer("coffee-finished", CoffeeFinished, finishCoffeeDuration)

    case CoffeeFinished if coffeeCounter > caffeineLimit => throw CaffeineException

    case CoffeeFinished =>
      orderCoffee
  }

  def orderCoffee = waiter ! Waiter.ServeCoffee(favouriteCoffee)
}
