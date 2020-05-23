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

  val prepareCoffeeDuration: FiniteDuration =
    context.system.settings.config.getDuration("coffee-house.barista.prepare-coffee-duration", MILLISECONDS).millis

  val finishCoffeeDuration: FiniteDuration =
    context.system.settings.config.getDuration("coffee-house.guest.finish-coffee-duration", MILLISECONDS).millis

  val barista = createBarista
  protected def createBarista: ActorRef =
    context.actorOf(Barista.props(prepareCoffeeDuration), "barista")

  val waiter = createWaiter(barista)
  protected def createWaiter(barista: ActorRef): ActorRef =
    context.actorOf(Waiter.props(barista), "waiter")

  protected def createGuest(favouriteCoffee: Coffee): ActorRef =
    context.actorOf(Guest.props(waiter, favouriteCoffee, finishCoffeeDuration))

  override def receive: Receive = {
    case CreateGuest(coffee) => createGuest(coffee)
  }
}
