package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Guest {
  case object CoffeeFinished
  def props(waiter: ActorRef, favouriteCoffee: Coffee): Props =
    Props(new Guest(waiter, favouriteCoffee))
}

class Guest(waiter: ActorRef, favouriteCoffee: Coffee) extends Actor with ActorLogging{

  import Guest._

  private var coffeeCounter = 0

  override def receive: Receive = {
    case Waiter.CoffeeServed(coffee) =>
      coffeeCounter += 1
      log.info(s"Enjoying my $coffeeCounter yummy $coffee")

    case CoffeeFinished =>
      waiter ! Waiter.ServeCoffee(favouriteCoffee)
  }
}
