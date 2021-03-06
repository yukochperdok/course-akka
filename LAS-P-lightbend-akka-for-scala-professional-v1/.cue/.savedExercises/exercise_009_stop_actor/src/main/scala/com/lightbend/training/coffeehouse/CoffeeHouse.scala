package com.lightbend.training.coffeehouse

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import scala.concurrent.duration._

object CoffeeHouse {

  case class CreateGuest(favouriteCoffee: Coffee)
  case class ApproveCoffee(coffee: Coffee, guest: ActorRef)

  def props(caffeineLimit: Int): Props = Props(new CoffeeHouse(caffeineLimit))
}

class CoffeeHouse(caffeineLimit: Int) extends Actor with ActorLogging{
  import CoffeeHouse._

  log.debug("CoffeeHouse Open")

  private var guestBook: Map[ActorRef, Int] = Map.empty.withDefaultValue(0)

  val prepareCoffeeDuration: FiniteDuration =
    context.system.settings.config.getDuration("coffee-house.barista.prepare-coffee-duration", MILLISECONDS).millis

  val finishCoffeeDuration: FiniteDuration =
    context.system.settings.config.getDuration("coffee-house.guest.finish-coffee-duration", MILLISECONDS).millis

  private val barista = createBarista
  protected def createBarista: ActorRef =
    context.actorOf(Barista.props(prepareCoffeeDuration), "barista")

  private val waiter = createWaiter
  protected def createWaiter: ActorRef =
    context.actorOf(Waiter.props(self), "waiter")

  protected def createGuest(favouriteCoffee: Coffee): ActorRef =
    context.actorOf(Guest.props(waiter, favouriteCoffee, finishCoffeeDuration))

  override def receive: Receive = {
    case CreateGuest(coffee) =>
      val guest = createGuest(coffee)
      guestBook += guest -> 0
      log.info(s"Guest ${guest} added to guest book")

    case ApproveCoffee(coffee, guest) if guestBook(guest) < caffeineLimit =>
      guestBook += guest -> (guestBook(guest) + 1)
      log.info(s"Guest $guest caffeine count incremented")
      barista.forward(Barista.PrepareCoffee(coffee, guest))

    case ApproveCoffee(_, guest) =>
      log.info(s"Sorry, $guest, but you have reached your limit")
      context.stop(guest)
  }
}
