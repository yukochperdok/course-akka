package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.concurrent.duration._

object CoffeeHouse {

  case class CreateGuest(favouriteCoffee: Coffee)

  def props: Props = Props(new CoffeeHouse)
}

class CoffeeHouse extends Actor with ActorLogging{
  import CoffeeHouse.CreateGuest
  log.debug("CoffeeHouse Open")

  val finishCoffeeDuration: FiniteDuration =
    context.system.settings.config.getDuration("coffee-house.guest.finish-coffee-duration", MILLISECONDS).millis

  val waiter = createWaiter
  protected def createWaiter: ActorRef = context.actorOf(Waiter.props, "waiter")

  protected def createGuest(favouriteCoffee: Coffee): ActorRef =
    context.actorOf(Guest.props(waiter, favouriteCoffee, finishCoffeeDuration))

  override def receive: Receive = {
    case CreateGuest(coffee) => createGuest(coffee)
  }
}
