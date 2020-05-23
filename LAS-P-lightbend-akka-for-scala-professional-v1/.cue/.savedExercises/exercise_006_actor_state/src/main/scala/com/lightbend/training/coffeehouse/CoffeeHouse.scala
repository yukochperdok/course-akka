package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object CoffeeHouse {

  case class CreateGuest(favouriteCoffee: Coffee)

  def props: Props = Props(new CoffeeHouse)
}

class CoffeeHouse extends Actor with ActorLogging{
  import CoffeeHouse.CreateGuest
  log.debug("CoffeeHouse Open")

  val waiter = createWaiter
  protected def createWaiter: ActorRef = context.actorOf(Waiter.props, "waiter")

  protected def createGuest(favouriteCoffee: Coffee): ActorRef =
    context.actorOf(Guest.props(waiter, favouriteCoffee))

  override def receive: Receive = {
    case CreateGuest(coffee) => createGuest(coffee)
  }
}
